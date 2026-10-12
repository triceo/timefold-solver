package ai.timefold.solver.core.impl.domain.variable;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import ai.timefold.solver.core.impl.domain.variable.descriptor.VariableDescriptor;
import ai.timefold.solver.core.impl.domain.variable.inverserelation.CollectionInverseVariableState;
import ai.timefold.solver.core.impl.domain.variable.inverserelation.InverseRelationShadowVariableDescriptor;
import ai.timefold.solver.core.impl.move.UndoableSupply;
import ai.timefold.solver.core.impl.score.director.InnerScoreDirector;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Single source of truth for the inverse relation of a basic {@link PlanningVariable}.
 * If the {@link InverseRelationShadowVariableDescriptor} is externalized,
 * there is a field on an entity holding the inverse collection and that field is used.
 * Otherwise, an internal map is used to track the inverse collection.
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
@NullMarked
public final class BasicVariableState<Solution_>
        implements BasicVariableChangeHandler<Solution_>, CollectionInverseVariableState, UndoableSupply {

    private static final int REMOVED = 0;
    private static final int ADDED = 1;

    private final VariableDescriptor<Solution_> sourceVariableDescriptor;
    private final Consumer<Object> notifier;

    @Nullable
    private InverseRelationShadowVariableDescriptor<Solution_> shadowVariableDescriptor;
    @SuppressWarnings("rawtypes")
    @Nullable
    private Map<Object, Set> inverseEntitySetMap;
    @Nullable
    private InnerScoreDirector<Solution_, ?> scoreDirector;

    BasicVariableState(VariableDescriptor<Solution_> sourceVariableDescriptor, Consumer<Object> notifier) {
        this.sourceVariableDescriptor = sourceVariableDescriptor;
        this.notifier = notifier;
    }

    public void externalize(InverseRelationShadowVariableDescriptor<Solution_> descriptor) {
        if (shadowVariableDescriptor != null) {
            throw new IllegalStateException(
                    "Impossible state: the sourceVariableDescriptor (%s) is already externalized by (%s); cannot externalize by (%s) as well."
                            .formatted(sourceVariableDescriptor, shadowVariableDescriptor, descriptor));
        }
        shadowVariableDescriptor = descriptor;
    }

    @Override
    public VariableDescriptor<Solution_> getSourceVariableDescriptor() {
        return sourceVariableDescriptor;
    }

    @Override
    public void resetWorkingSolution(InnerScoreDirector<Solution_, ?> scoreDirector) {
        this.scoreDirector = scoreDirector;
        if (shadowVariableDescriptor == null) {
            inverseEntitySetMap = new IdentityHashMap<>();
            sourceVariableDescriptor.getEntityDescriptor().visitAllEntities(scoreDirector.getWorkingSolution(),
                    entity -> insert(scoreDirector, entity));
        } else {
            forEachEntity(scoreDirector, shadowVariableDescriptor.getEntityDescriptor().getEntityClass(),
                    value -> getInverseCollection(value).clear());
            forEachEntity(scoreDirector, sourceVariableDescriptor.getEntityDescriptor().getEntityClass(),
                    entity -> insert(scoreDirector, entity));
        }
    }

    @SuppressWarnings("unchecked")
    private static <Solution_, Entity_> void forEachEntity(InnerScoreDirector<Solution_, ?> scoreDirector,
            Class<? extends Entity_> entityClass,
            Consumer<Entity_> entityConsumer) {
        scoreDirector.getSolutionDescriptor().visitEntitiesByEntityClass(scoreDirector.getWorkingSolution(),
                entityClass, entity -> {
                    entityConsumer.accept((Entity_) entity);
                    return false;
                });
    }

    @Override
    public void beforeVariableChanged(InnerScoreDirector<Solution_, ?> scoreDirector, Object entity) {
        retract(scoreDirector, entity);
    }

    @Override
    public void afterVariableChanged(InnerScoreDirector<Solution_, ?> scoreDirector, Object entity) {
        insert(scoreDirector, entity);
    }

    @Override
    public void close() {
        if (shadowVariableDescriptor == null) {
            inverseEntitySetMap = null;
        }
    }

    private void insert(InnerScoreDirector<Solution_, ?> scoreDirector, Object entity) {
        if (shadowVariableDescriptor == null) {
            var value = sourceVariableDescriptor.getValue(entity);
            if (value == null) {
                return;
            }
            var inverseEntitySet =
                    inverseEntitySetMap.computeIfAbsent(value, k -> Collections.newSetFromMap(new IdentityHashMap<>()));
            var addSucceeded = inverseEntitySet.add(entity);
            if (!addSucceeded) {
                throw new IllegalStateException(
                        "The supply (%s) is corrupted, because the entity (%s) for sourceVariable (%s) was already inserted."
                                .formatted(this, entity, sourceVariableDescriptor.getVariableName()));
            }
            recordChange(scoreDirector, value, entity, ADDED);
            notifier.accept(value);
        } else {
            var shadowEntity = sourceVariableDescriptor.getValue(entity);
            if (shadowEntity != null) {
                Collection<Object> shadowCollection = shadowVariableDescriptor.getValue(shadowEntity);
                if (scoreDirector.expectShadowVariablesInCorrectState() && shadowCollection == null) {
                    throw new IllegalStateException("""
                            The entity (%s) has a variable (%s) with value (%s) which has a sourceVariableName variable (%s) \
                            with a value (%s) which is null.
                            Verify the consistency of your input problem for that bi-directional relationship.
                            Non-singleton inverse variable can never be null, at the very least it should be an empty %s."""
                            .formatted(entity, sourceVariableDescriptor.getVariableName(), shadowEntity,
                                    shadowVariableDescriptor.getVariableName(), shadowCollection,
                                    Collection.class.getSimpleName()));
                }
                scoreDirector.beforeVariableChanged(shadowVariableDescriptor, shadowEntity);
                var added = shadowCollection.add(entity);
                if (scoreDirector.expectShadowVariablesInCorrectState() && !added) {
                    throw new IllegalStateException("""
                            The entity (%s) has a variable (%s) with value (%s) which has a sourceVariableName variable (%s) \
                            with a value (%s) which already contained the entity (%s).
                            Verify the consistency of your input problem for that bi-directional relationship."""
                            .formatted(entity, sourceVariableDescriptor.getVariableName(), shadowEntity,
                                    shadowVariableDescriptor.getVariableName(), shadowCollection, entity));
                }
                if (added) {
                    recordChange(scoreDirector, shadowEntity, entity, ADDED);
                }
                scoreDirector.afterVariableChanged(shadowVariableDescriptor, shadowEntity);
            }
        }
    }

    private void retract(InnerScoreDirector<Solution_, ?> scoreDirector, Object entity) {
        if (shadowVariableDescriptor == null) {
            var value = sourceVariableDescriptor.getValue(entity);
            if (value == null) {
                return;
            }
            var inverseEntitySet = inverseEntitySetMap.get(value);
            var removeSucceeded = inverseEntitySet.remove(entity);
            if (!removeSucceeded) {
                throw new IllegalStateException(
                        "The supply (%s) is corrupted, because the entity (%s) for sourceVariable (%s) was never inserted."
                                .formatted(this, entity, sourceVariableDescriptor.getVariableName()));
            }
            if (inverseEntitySet.isEmpty()) {
                inverseEntitySetMap.put(value, null);
            }
            recordChange(scoreDirector, value, entity, REMOVED);
            notifier.accept(value);
        } else {
            var shadowEntity = sourceVariableDescriptor.getValue(entity);
            if (shadowEntity != null) {
                Collection<Object> shadowCollection = shadowVariableDescriptor.getValue(shadowEntity);
                if (scoreDirector.expectShadowVariablesInCorrectState() && shadowCollection == null) {
                    throw new IllegalStateException("""
                            The entity (%s) has a variable (%s) with value (%s) which has a sourceVariableName variable (%s) \
                            with a value (%s) which is null.
                            Verify the consistency of your input problem for that bi-directional relationship.
                            Non-singleton inverse variable can never be null, at the very least it should be an empty %s."""
                            .formatted(entity, sourceVariableDescriptor.getVariableName(), shadowEntity,
                                    shadowVariableDescriptor.getVariableName(), shadowCollection,
                                    Collection.class.getSimpleName()));
                }
                scoreDirector.beforeVariableChanged(shadowVariableDescriptor, shadowEntity);
                var removed = shadowCollection.remove(entity);
                if (scoreDirector.expectShadowVariablesInCorrectState() && !removed) {
                    throw new IllegalStateException("""
                            The entity (%s) has a variable (%s) with value (%s) which has a sourceVariableName variable (%s) \
                            with a value (%s) which did not contain the entity (%s)
                            Verify the consistency of your input problem for that bi-directional relationship."""
                            .formatted(entity, sourceVariableDescriptor.getVariableName(), shadowEntity,
                                    shadowVariableDescriptor.getVariableName(), shadowCollection, entity));
                }
                if (removed) {
                    recordChange(scoreDirector, shadowEntity, entity, REMOVED);
                }
                scoreDirector.afterVariableChanged(shadowVariableDescriptor, shadowEntity);
            }
        }
    }

    private void recordChange(InnerScoreDirector<Solution_, ?> scoreDirector, Object value, Object entity, int kind) {
        var journal = scoreDirector.getUndoJournal();
        if (journal != null) {
            journal.recordSupplyChange(this, value, entity, kind);
        }
    }

    /**
     * Reverts one add or remove of an entity in the inverse collection of a value.
     * Runs while the score director restores, so the events around the shadow collection skip the variable support.
     *
     * @param value the value whose inverse collection changed
     * @param entity the entity that was added or removed
     * @param kind {@link #ADDED} or {@link #REMOVED}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void undo(Object value, @Nullable Object entity, int kind) {
        var director = Objects.requireNonNull(scoreDirector);
        var add = kind == REMOVED;
        recordChange(director, value, Objects.requireNonNull(entity), add ? ADDED : REMOVED);
        if (shadowVariableDescriptor == null) {
            if (add) {
                inverseEntitySetMap.computeIfAbsent(value, k -> Collections.newSetFromMap(new IdentityHashMap<>()))
                        .add(entity);
            } else {
                var inverseEntitySet = inverseEntitySetMap.get(value);
                inverseEntitySet.remove(entity);
                if (inverseEntitySet.isEmpty()) {
                    inverseEntitySetMap.put(value, null);
                }
            }
            notifier.accept(value);
        } else {
            Collection<Object> shadowCollection = shadowVariableDescriptor.getValue(value);
            director.beforeVariableChanged(shadowVariableDescriptor, value);
            if (add) {
                shadowCollection.add(entity);
            } else {
                shadowCollection.remove(entity);
            }
            director.afterVariableChanged(shadowVariableDescriptor, value);
        }
    }

    /**
     * Compares the inverse collections with the genuine values.
     *
     * @param violationList receives one message per violation
     */
    void collectViolations(InnerScoreDirector<Solution_, ?> scoreDirector, List<String> violationList) {
        var expectedSizeMap = new IdentityHashMap<Object, Integer>();
        Consumer<Object> entityVisitor = entity -> {
            var value = sourceVariableDescriptor.getValue(entity);
            if (value == null) {
                return;
            }
            expectedSizeMap.merge(value, 1, Integer::sum);
            if (!getInverseCollection(value).contains(entity)) {
                violationList.add("The inverse collection of value (%s) for variable (%s) misses the entity (%s)."
                        .formatted(value, sourceVariableDescriptor.getVariableName(), entity));
            }
        };
        var shadowDescriptor = shadowVariableDescriptor;
        if (shadowDescriptor == null) {
            sourceVariableDescriptor.getEntityDescriptor().visitAllEntities(scoreDirector.getWorkingSolution(),
                    entityVisitor);
            Objects.requireNonNull(inverseEntitySetMap)
                    .forEach((value, set) -> checkSize(value, set, expectedSizeMap, violationList));
        } else {
            forEachEntity(scoreDirector, sourceVariableDescriptor.getEntityDescriptor().getEntityClass(), entityVisitor);
            forEachEntity(scoreDirector, shadowDescriptor.getEntityDescriptor().getEntityClass(),
                    value -> checkSize(value, shadowDescriptor.getValue(value), expectedSizeMap, violationList));
        }
    }

    private void checkSize(Object value, @Nullable Collection<?> inverseCollection, Map<Object, Integer> expectedSizeMap,
            List<String> violationList) {
        var actualSize = inverseCollection == null ? 0 : inverseCollection.size();
        var expectedSize = expectedSizeMap.getOrDefault(value, 0);
        if (actualSize != expectedSize) {
            violationList.add("The inverse collection of value (%s) for variable (%s) has size (%d) instead of (%d)."
                    .formatted(value, sourceVariableDescriptor.getVariableName(), actualSize, expectedSize));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Entity_> Collection<Entity_> getInverseCollection(Object planningValue) {
        if (shadowVariableDescriptor == null) {
            var inverseEntitySet = inverseEntitySetMap.get(planningValue);
            return Objects.requireNonNullElse(inverseEntitySet, Collections.emptySet());
        } else {
            return shadowVariableDescriptor.getValue(planningValue);
        }
    }

    @Override
    public String toString() {
        return "%s(%s)".formatted(getClass().getSimpleName(), sourceVariableDescriptor.getVariableName());
    }

}
