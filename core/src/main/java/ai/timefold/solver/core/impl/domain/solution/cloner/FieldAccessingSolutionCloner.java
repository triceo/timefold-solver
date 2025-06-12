package ai.timefold.solver.core.impl.domain.solution.cloner;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import ai.timefold.solver.core.api.domain.solution.cloner.DeepPlanningClone;
import ai.timefold.solver.core.api.domain.solution.cloner.SolutionCloner;
import ai.timefold.solver.core.impl.domain.common.accessor.MemberAccessor;
import ai.timefold.solver.core.impl.domain.solution.descriptor.SolutionDescriptor;
import ai.timefold.solver.core.impl.util.CollectionUtils;

import org.jspecify.annotations.NonNull;

/**
 * This class is thread-safe; score directors from the same solution descriptor will share the same instance.
 */
public final class FieldAccessingSolutionCloner<Solution_> implements SolutionCloner<Solution_> {

    // Too big for small solutions, but helps with cloning larger solutions,
    // where performance is an actual concern.
    private static final int EXPECTED_OBJECT_COUNT = 10_000;

    private final SolutionDescriptor<Solution_> solutionDescriptor;
    private final Map<Class<?>, MethodHandle> constructorMemoization = new IdentityHashMap<>();
    private final Map<Class<?>, ClassMetadata> classMetadataMemoization = new IdentityHashMap<>();

    public FieldAccessingSolutionCloner(SolutionDescriptor<Solution_> solutionDescriptor) {
        this.solutionDescriptor = solutionDescriptor;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public @NonNull Solution_ cloneSolution(@NonNull Solution_ originalSolution) {
        var originalToCloneMap = CollectionUtils.newIdentityHashMap(EXPECTED_OBJECT_COUNT);
        var unprocessedQueue = new ArrayDeque<Unprocessed>(EXPECTED_OBJECT_COUNT);
        var cloneSolution = clone(originalSolution, originalToCloneMap, unprocessedQueue,
                retrieveClassMetadata(originalSolution.getClass()));
        while (!unprocessedQueue.isEmpty()) {
            var unprocessed = unprocessedQueue.remove();
            var cloneValue = process(unprocessed, originalToCloneMap, unprocessedQueue);
            FieldCloningUtils.setObjectFieldValue(unprocessed.bean, unprocessed.field, cloneValue);
        }
        validateCloneSolution(originalSolution, cloneSolution);
        return cloneSolution;
    }

    /**
     * Used by GIZMO when it encounters an undeclared entity class, such as when an abstract planning entity is extended.
     */
    @SuppressWarnings("unused")
    public Object gizmoFallbackDeepClone(Object originalValue, Map<Object, Object> originalToCloneMap) {
        if (originalValue == null) {
            return null;
        }
        var unprocessedQueue = new ArrayDeque<Unprocessed>();
        var fieldType = originalValue.getClass();
        return clone(originalValue, originalToCloneMap, unprocessedQueue, fieldType);
    }

    private Object clone(Object originalValue, Map<Object, Object> originalToCloneMap, Queue<Unprocessed> unprocessedQueue,
            Class<?> fieldType) {
        if (originalValue instanceof Collection<?> collection) {
            return cloneCollection(fieldType, collection, originalToCloneMap, unprocessedQueue);
        } else if (originalValue instanceof Map<?, ?> map) {
            return cloneMap(fieldType, map, originalToCloneMap, unprocessedQueue);
        } else if (originalValue.getClass().isArray()) {
            return cloneArray(fieldType, originalValue, originalToCloneMap, unprocessedQueue);
        } else {
            return clone(originalValue, originalToCloneMap, unprocessedQueue,
                    retrieveClassMetadata(originalValue.getClass()));
        }
    }

    private Object process(Unprocessed unprocessed, Map<Object, Object> originalToCloneMap,
            Queue<Unprocessed> unprocessedQueue) {
        var originalValue = unprocessed.originalValue;
        var field = unprocessed.field;
        var fieldType = field.getType();
        return clone(originalValue, originalToCloneMap, unprocessedQueue, fieldType);
    }

    @SuppressWarnings("unchecked")
    private <C> C clone(C original, Map<Object, Object> originalToCloneMap, Queue<Unprocessed> unprocessedQueue,
            ClassMetadata declaringClassMetadata) {
        if (original == null) {
            return null;
        }
        var existingClone = (C) originalToCloneMap.get(original);
        if (existingClone != null) {
            return existingClone;
        }

        var declaringClass = (Class<C>) original.getClass();
        var clone = constructClone(original, declaringClass);
        originalToCloneMap.put(original, clone);
        copyFields(declaringClass, original, clone, unprocessedQueue, declaringClassMetadata);
        return clone;
    }

    @SuppressWarnings("unchecked")
    private <C> C constructClone(C original, Class<C> declaringClass) {
        if (original instanceof PlanningCloneable<?> planningCloneable) {
            return (C) planningCloneable.createNewInstance();
        } else {
            return constructClone(declaringClass);
        }
    }

    @SuppressWarnings("unchecked")
    private <C> C constructClone(Class<C> clazz) {
        var constructor = retrieveConstructor(clazz);
        try {
            return (C) constructor.invoke();
        } catch (Throwable e) {
            throw new IllegalStateException(
                    "Can not create a new instance of class (%s) for a planning clone, using its no-arg constructor."
                            .formatted(clazz.getCanonicalName()),
                    e);
        }
    }

    private MethodHandle retrieveConstructor(Class<?> clazz) {
        synchronized (constructorMemoization) {
            var cachedCtor = constructorMemoization.get(clazz);
            if (cachedCtor == null) {
                try {
                    var ctor = clazz.getDeclaredConstructor();
                    ctor.setAccessible(true);
                    cachedCtor = MethodHandles.lookup()
                            .unreflectConstructor(ctor);
                    constructorMemoization.put(clazz, cachedCtor);
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("To create a planning clone, the class (%s) must have a no-arg constructor."
                            .formatted(clazz.getCanonicalName()),
                            e);
                }
            }
            return cachedCtor;
        }
    }

    private <C> void copyFields(Class<C> clazz, C original, C clone, Queue<Unprocessed> unprocessedQueue,
            ClassMetadata declaringClassMetadata) {
        for (var fieldCloner : declaringClassMetadata.getCopiedFieldArray()) {
            fieldCloner.clone(original, clone);
        }
        for (var fieldCloner : declaringClassMetadata.getClonedFieldArray()) {
            var unprocessedValue = fieldCloner.clone(solutionDescriptor, original, clone);
            if (unprocessedValue != null) {
                unprocessedQueue.add(new Unprocessed(clone, fieldCloner.getField(), unprocessedValue));
            }
        }
        var superclass = clazz.getSuperclass();
        if (superclass != null && superclass != Object.class) {
            copyFields(superclass, original, clone, unprocessedQueue, retrieveClassMetadata(superclass));
        }
    }

    private Object cloneArray(Class<?> expectedType, Object originalArray, Map<Object, Object> originalToCloneMap,
            Queue<Unprocessed> unprocessedQueue) {
        var arrayLength = Array.getLength(originalArray);
        var cloneArray = Array.newInstance(originalArray.getClass().getComponentType(), arrayLength);
        if (!expectedType.isInstance(cloneArray)) {
            throw new IllegalStateException("""
                    The cloneArrayClass (%s) created for originalArrayClass (%s) is not assignable to the field's type (%s).
                    Maybe consider replacing the default %s."""
                    .formatted(cloneArray.getClass(), originalArray.getClass(), expectedType,
                            SolutionCloner.class.getSimpleName()));
        }
        for (var i = 0; i < arrayLength; i++) {
            var cloneElement =
                    cloneCollectionsElementIfNeeded(Array.get(originalArray, i), originalToCloneMap, unprocessedQueue);
            Array.set(cloneArray, i, cloneElement);
        }
        return cloneArray;
    }

    private <E> Collection<E> cloneCollection(Class<?> expectedType, Collection<E> originalCollection,
            Map<Object, Object> originalToCloneMap, Queue<Unprocessed> unprocessedQueue) {
        var cloneCollection = constructCloneCollection(originalCollection);
        if (!expectedType.isInstance(cloneCollection)) {
            throw new IllegalStateException(
                    """
                            The cloneCollectionClass (%s) created for originalCollectionClass (%s) is not assignable to the field's type (%s).
                            Maybe consider replacing the default %s."""
                            .formatted(cloneCollection.getClass(), originalCollection.getClass(), expectedType,
                                    SolutionCloner.class.getSimpleName()));
        }
        for (var originalElement : originalCollection) {
            var cloneElement = cloneCollectionsElementIfNeeded(originalElement, originalToCloneMap, unprocessedQueue);
            cloneCollection.add(cloneElement);
        }
        return cloneCollection;
    }

    @SuppressWarnings("unchecked")
    private static <E> Collection<E> constructCloneCollection(Collection<E> originalCollection) {
        // TODO Don't hardcode all standard collections
        if (originalCollection instanceof PlanningCloneable<?> planningCloneable) {
            return (Collection<E>) planningCloneable.createNewInstance();
        }
        if (originalCollection instanceof LinkedList) {
            return new LinkedList<>();
        }
        var size = originalCollection.size();
        if (originalCollection instanceof Set) {
            if (originalCollection instanceof SortedSet<E> set) {
                var setComparator = set.comparator();
                return new TreeSet<>(setComparator);
            } else if (!(originalCollection instanceof LinkedHashSet)) {
                return new HashSet<>(size);
            } else { // Default to a LinkedHashSet to respect order.
                return new LinkedHashSet<>(size);
            }
        } else if (originalCollection instanceof Deque) {
            return new ArrayDeque<>(size);
        }
        // Default collection
        return new ArrayList<>(size);
    }

    private <K, V> Map<K, V> cloneMap(Class<?> expectedType, Map<K, V> originalMap, Map<Object, Object> originalToCloneMap,
            Queue<Unprocessed> unprocessedQueue) {
        var cloneMap = constructCloneMap(originalMap);
        if (!expectedType.isInstance(cloneMap)) {
            throw new IllegalStateException("""
                    The cloneMapClass (%s) created for originalMapClass (%s) is not assignable to the field's type (%s).
                    Maybe consider replacing the default %s."""
                    .formatted(cloneMap.getClass(), originalMap.getClass(), expectedType,
                            SolutionCloner.class.getSimpleName()));
        }
        for (var originalEntry : originalMap.entrySet()) {
            var cloneKey = cloneCollectionsElementIfNeeded(originalEntry.getKey(), originalToCloneMap, unprocessedQueue);
            var cloneValue = cloneCollectionsElementIfNeeded(originalEntry.getValue(), originalToCloneMap, unprocessedQueue);
            cloneMap.put(cloneKey, cloneValue);
        }
        return cloneMap;
    }

    @SuppressWarnings("unchecked")
    private static <K, V> Map<K, V> constructCloneMap(Map<K, V> originalMap) {
        // Normally, a Map will never be selected for cloning, but extending implementations might anyway.
        if (originalMap instanceof PlanningCloneable<?> planningCloneable) {
            return (Map<K, V>) planningCloneable.createNewInstance();
        }
        if (originalMap instanceof SortedMap<K, V> map) {
            var comparator = map.comparator();
            return new TreeMap<>(comparator);
        }
        var originalMapSize = originalMap.size();
        if (originalMap instanceof LinkedHashMap) { // Default to a LinkedHashMap to respect order.
            return new LinkedHashMap<>(originalMapSize);
        } else {
            return new HashMap<>(originalMapSize);
        }
    }

    private ClassMetadata retrieveClassMetadata(Class<?> declaringClass) {
        synchronized (classMetadataMemoization) {
            var cachedMetadata = classMetadataMemoization.get(declaringClass);
            if (cachedMetadata == null) {
                cachedMetadata = new ClassMetadata(solutionDescriptor, declaringClass);
                classMetadataMemoization.put(declaringClass, cachedMetadata);
            }
            return cachedMetadata;
        }
    }

    @SuppressWarnings("unchecked")
    private <C> C cloneCollectionsElementIfNeeded(C original, Map<Object, Object> originalToCloneMap,
            Queue<Unprocessed> unprocessedQueue) {
        if (original == null) {
            return null;
        }
        /*
         * Because an element which is itself a Collection or Map might hold an entity,
         * we clone it too.
         * The List<Long> in Map<String, List<Long>> needs to be cloned if the List<Long> is a shadow,
         * despite that Long never needs to be cloned (because it's immutable).
         */
        if (original instanceof Collection<?> collection) {
            return (C) cloneCollection(Collection.class, collection, originalToCloneMap, unprocessedQueue);
        } else if (original instanceof Map<?, ?> map) {
            return (C) cloneMap(Map.class, map, originalToCloneMap, unprocessedQueue);
        } else if (original.getClass().isArray()) {
            return (C) cloneArray(original.getClass(), original, originalToCloneMap, unprocessedQueue);
        }
        var classMetadata = retrieveClassMetadata(original.getClass());
        if (classMetadata.isDeepCloned) {
            return clone(original, originalToCloneMap, unprocessedQueue, classMetadata);
        } else {
            return original;
        }
    }

    /**
     * Fails fast if {@link DeepCloningUtils#isFieldAnEntityPropertyOnSolution} assumptions were wrong.
     *
     * @param originalSolution never null
     * @param cloneSolution never null
     */
    private void validateCloneSolution(Solution_ originalSolution, Solution_ cloneSolution) {
        for (var memberAccessor : solutionDescriptor.getEntityMemberAccessorMap().values()) {
            validateCloneProperty(originalSolution, cloneSolution, memberAccessor);
        }
        for (var memberAccessor : solutionDescriptor.getEntityCollectionMemberAccessorMap().values()) {
            validateCloneProperty(originalSolution, cloneSolution, memberAccessor);
        }
    }

    private static <Solution_> void validateCloneProperty(Solution_ originalSolution, Solution_ cloneSolution,
            MemberAccessor memberAccessor) {
        var originalProperty = memberAccessor.executeGetter(originalSolution);
        if (originalProperty != null) {
            var cloneProperty = memberAccessor.executeGetter(cloneSolution);
            if (originalProperty == cloneProperty) {
                throw new IllegalStateException("""
                        The solutionProperty (%s) was not cloned as expected.
                        The %s failed to recognize that property's field, probably because its field name is different."""
                        .formatted(memberAccessor.getName(), FieldAccessingSolutionCloner.class.getSimpleName()));
            }
        }
    }

    private static final class ClassMetadata {

        private final SolutionDescriptor<?> solutionDescriptor;
        private final Class<?> declaringClass;
        private final boolean isDeepCloned;

        /**
         * Contains one cloner for every field that needs to be shallow cloned (= copied).
         */
        private ShallowCloningFieldCloner[] copiedFieldArray;
        /**
         * Contains one cloner for every field that needs to be deep-cloned.
         */
        private DeepCloningFieldCloner[] clonedFieldArray;

        public ClassMetadata(SolutionDescriptor<?> solutionDescriptor, Class<?> declaringClass) {
            this.solutionDescriptor = solutionDescriptor;
            this.declaringClass = declaringClass;
            this.isDeepCloned = DeepCloningUtils.isClassDeepCloned(solutionDescriptor, declaringClass);
        }

        public ShallowCloningFieldCloner[] getCopiedFieldArray() {
            if (copiedFieldArray == null) { // Lazy-loaded; some types (such as String) will never get here.
                copiedFieldArray = Arrays.stream(declaringClass.getDeclaredFields())
                        .filter(f -> !Modifier.isStatic(f.getModifiers()))
                        .filter(field -> DeepCloningUtils.isImmutable(field.getType()))
                        .peek(f -> {
                            if (DeepCloningUtils.needsDeepClone(solutionDescriptor, f, declaringClass)) {
                                throw new IllegalStateException("""
                                        The field (%s) of class (%s) needs to be deep-cloned,
                                        but its type (%s) is immutable and can not be deep-cloned.
                                        Maybe remove the @%s annotation from the field?
                                        Maybe do not reference planning entities inside Java records?"""
                                        .formatted(f.getName(), declaringClass.getCanonicalName(),
                                                f.getType().getCanonicalName(), DeepPlanningClone.class.getSimpleName()));
                            } else {
                                f.setAccessible(true);
                            }
                        })
                        .map(ShallowCloningFieldCloner::of)
                        .toArray(ShallowCloningFieldCloner[]::new);
            }
            return copiedFieldArray;
        }

        public DeepCloningFieldCloner[] getClonedFieldArray() {
            if (clonedFieldArray == null) { // Lazy-loaded; some types (such as String) will never get here.
                clonedFieldArray = Arrays.stream(declaringClass.getDeclaredFields())
                        .filter(f -> !Modifier.isStatic(f.getModifiers()))
                        .filter(field -> !DeepCloningUtils.isImmutable(field.getType()))
                        .peek(f -> f.setAccessible(true))
                        .map(DeepCloningFieldCloner::new)
                        .toArray(DeepCloningFieldCloner[]::new);
            }
            return clonedFieldArray;
        }

    }

    private record Unprocessed(Object bean, Field field, Object originalValue) {
    }

}
