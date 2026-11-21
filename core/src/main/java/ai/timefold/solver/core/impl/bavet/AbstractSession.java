package ai.timefold.solver.core.impl.bavet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.impl.bavet.common.AbstractPrecomputeNode;
import ai.timefold.solver.core.impl.bavet.common.AbstractRootNode;
import ai.timefold.solver.core.impl.bavet.common.tuple.UniTuple;
import ai.timefold.solver.core.impl.bavet.uni.AbstractForEachUniNode;
import ai.timefold.solver.core.impl.domain.variable.descriptor.VariableDescriptor;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Feeds facts into the root nodes of a node network.
 *
 * <p>
 * Each concrete fact class has one {@link ClassPlan}: the root nodes which accept the class,
 * and the tuple which each for-each node holds for each fact of the class.
 * Inserts, retracts and {@link #update(Object)} find the plan by class in {@link #classPlanMap}.
 * Entity updates from the score director, {@link #update(Object, VariableDescriptor)}, are the hot path;
 * they find the plan by the ordinal of the entity descriptor in {@link #entityClassPlans}, with no map lookup.
 *
 * <p>
 * A plan keeps its tuples in one of two ways:
 * <ul>
 * <li>Immediate, the default and the only way for problem facts:
 * one fact-to-tuple map for each for-each node.
 * An update looks the fact up in each map and updates each node at once.</li>
 * <li>Deferred, for the classes in {@link #deferredUpdateClassSet}, such as entities with declarative shadow
 * variables: one {@link FactHandle} for each fact, with the tuples of all its for-each nodes.
 * An update only marks the handle dirty, and {@link #settle()} updates each dirty fact once in each node.
 * This pays off when a fact receives many updates between two settles,
 * but costs a second pass over the facts.</li>
 * </ul>
 * The two ways have separate code paths, because one shared path measurably slowed down immediate updates.
 * Precompute nodes are not for-each nodes; they receive every insert, update and retract at once.
 */
@NullMarked
public abstract class AbstractSession<Network_ extends AbstractBavetNodeNetwork> {

    /**
     * Value of a fact map for a fact which its node holds no tuple for.
     */
    private static final Object NO_TUPLE = new Object();

    protected final Network_ nodeNetwork;
    private final Set<Class<?>> deferredUpdateClassSet;
    private final Map<Class<?>, ClassPlan> classPlanMap;
    private final List<FactHandle> dirtyHandleList = new ArrayList<>();
    /**
     * Index is the ordinal of the entity descriptor; avoids the map lookup in {@link #update(Object, VariableDescriptor)}.
     */
    private @Nullable ClassPlan[] entityClassPlans = new ClassPlan[0];
    private boolean initialized = false;
    private boolean settled = false;

    protected AbstractSession(Network_ nodeNetwork) {
        this(nodeNetwork, Set.of());
    }

    /**
     * @param deferredUpdateClassSet updates of facts of these classes and their subclasses wait until settle;
     *        worth it for classes whose facts receive many updates between two settles
     */
    protected AbstractSession(Network_ nodeNetwork, Set<Class<?>> deferredUpdateClassSet) {
        this.nodeNetwork = nodeNetwork;
        this.deferredUpdateClassSet = deferredUpdateClassSet;
        this.classPlanMap = HashMap.newHashMap(nodeNetwork.forEachNodeCount());
    }

    public final void insert(Object fact) {
        settled = false;
        var plan = getClassPlan(fact.getClass());
        for (var node : plan.otherNodes) {
            node.insert(fact);
        }
        if (plan.deferred) {
            insertDeferred(plan, fact);
            return;
        }
        var nodes = plan.forEachNodes;
        for (var i = 0; i < nodes.length; i++) {
            var tuple = nodes[i].insertFact(fact);
            if (plan.maps[i].putIfAbsent(fact, tuple == null ? NO_TUPLE : tuple) != null) {
                throw alreadyInserted(fact);
            }
        }
    }

    private static void insertDeferred(ClassPlan plan, Object fact) {
        var handle = new FactHandle(fact, plan);
        if (plan.handleMap.putIfAbsent(fact, handle) != null) {
            throw alreadyInserted(fact);
        }
        var nodes = plan.forEachNodes;
        for (var i = 0; i < nodes.length; i++) {
            if (plan.active[i]) {
                handle.tuples[i] = nodes[i].insertFact(fact);
            }
        }
    }

    public final void update(Object fact) {
        updatePlan(getClassPlan(fact.getClass()), fact);
    }

    /**
     * As {@link #update(Object)}, but finds the root nodes by the ordinal of the entity descriptor.
     */
    public final void update(Object entity, VariableDescriptor<?> variableDescriptor) {
        var entityOrdinal = variableDescriptor.getEntityDescriptor().getOrdinal();
        var plans = entityClassPlans;
        if (entityOrdinal < plans.length) {
            var plan = plans[entityOrdinal];
            if (plan != null && plan.factClass == entity.getClass()) {
                updatePlan(plan, entity);
                return;
            }
        }
        updatePlan(getEntityClassPlan(entity, entityOrdinal), entity);
    }

    private ClassPlan getEntityClassPlan(Object entity, int entityOrdinal) {
        var plan = getClassPlan(entity.getClass());
        if (entityOrdinal >= entityClassPlans.length) {
            entityClassPlans = Arrays.copyOf(entityClassPlans, entityOrdinal + 1);
        }
        if (entityClassPlans[entityOrdinal] == null) { // The first class wins; its subclasses use the map.
            entityClassPlans[entityOrdinal] = plan;
        }
        return plan;
    }

    @SuppressWarnings("unchecked")
    private void updatePlan(ClassPlan plan, Object fact) {
        settled = false;
        for (var node : plan.otherNodes) {
            node.update(fact);
        }
        if (plan.deferred) {
            updateDeferred(plan, fact);
            return;
        }
        var nodes = plan.forEachNodes;
        var maps = plan.maps;
        for (var i = 0; i < nodes.length; i++) {
            var stored = maps[i].get(fact);
            if (stored == null) {
                throw neverInserted(fact);
            }
            var tuple = stored == NO_TUPLE ? null : (UniTuple<Object>) stored;
            var updated = nodes[i].updateFact(fact, tuple);
            if (updated != tuple) {
                maps[i].put(fact, updated == null ? NO_TUPLE : updated);
            }
        }
    }

    private void updateDeferred(ClassPlan plan, Object fact) {
        var handle = plan.handleMap.get(fact);
        if (handle == null) {
            throw neverInserted(fact);
        }
        if (!handle.dirty) {
            handle.dirty = true;
            dirtyHandleList.add(handle);
        }
    }

    @SuppressWarnings("unchecked")
    public final void retract(Object fact) {
        settled = false;
        var plan = getClassPlan(fact.getClass());
        for (var node : plan.otherNodes) {
            node.retract(fact);
        }
        if (plan.deferred) {
            retractDeferred(plan, fact);
            return;
        }
        var nodes = plan.forEachNodes;
        for (var i = 0; i < nodes.length; i++) {
            var stored = plan.maps[i].remove(fact);
            if (stored == null) {
                throw neverInserted(fact);
            }
            nodes[i].retractFact(fact, stored == NO_TUPLE ? null : (UniTuple<Object>) stored);
        }
    }

    private static void retractDeferred(ClassPlan plan, Object fact) {
        var handle = plan.handleMap.remove(fact);
        if (handle == null) {
            throw neverInserted(fact);
        }
        handle.retracted = true;
        var nodes = plan.forEachNodes;
        for (var i = 0; i < nodes.length; i++) {
            if (plan.active[i]) {
                nodes[i].retractFact(fact, handle.tuples[i]);
            }
        }
    }

    private static IllegalStateException alreadyInserted(Object fact) {
        return new IllegalStateException("""
                The fact (%s) was already inserted.
                Maybe your entities mistakenly equal each other?
                Maybe your @%s's collection properties contain duplicates?"""
                .formatted(fact, PlanningSolution.class.getSimpleName()));
    }

    private static IllegalStateException neverInserted(Object fact) {
        return new IllegalStateException("The fact (%s) was never inserted."
                .formatted(fact));
    }

    private ClassPlan getClassPlan(Class<?> factClass) {
        // Map.computeIfAbsent() would have created lambdas on the hot path, this will not.
        var plan = classPlanMap.get(factClass);
        if (plan == null) {
            var deferred = deferredUpdateClassSet.stream().anyMatch(clazz -> clazz.isAssignableFrom(factClass));
            plan = new ClassPlan(factClass, nodeNetwork.getRootNodesAcceptingType(factClass).toList(), deferred);
            classPlanMap.put(factClass, plan);
        }
        return plan;
    }

    public final void settle() {
        if (settled) {
            return;
        }
        if (!dirtyHandleList.isEmpty()) {
            flushUpdates();
        }
        nodeNetwork.settle();
        if (!initialized && nodeNetwork.isActivationCheckComplete()) {
            var activeNodeSet = nodeNetwork.getActiveNodes();
            for (var plan : classPlanMap.values()) {
                plan.removeInactiveNodes(activeNodeSet::contains);
            }
            initialized = true;
        }
        settled = true;
    }

    private void flushUpdates() {
        for (var handleIndex = 0; handleIndex < dirtyHandleList.size(); handleIndex++) {
            var handle = dirtyHandleList.get(handleIndex);
            handle.dirty = false;
            if (handle.retracted) {
                continue;
            }
            var plan = handle.plan;
            var nodes = plan.forEachNodes;
            var tuples = handle.tuples;
            for (var i = 0; i < nodes.length; i++) {
                if (plan.active[i]) {
                    tuples[i] = nodes[i].updateFact(handle.fact, tuples[i]);
                }
            }
        }
        dirtyHandleList.clear();
    }

    public Network_ getNodeNetwork() {
        return nodeNetwork;
    }

    /**
     * Root nodes of one concrete fact class, and the tuples of its facts; see {@link AbstractSession}.
     */
    private static final class ClassPlan {

        private final Class<?> factClass;
        private final boolean deferred;
        private AbstractForEachUniNode<Object>[] forEachNodes;
        /**
         * Immediate classes only; index matches {@link #forEachNodes}, value is the tuple or {@link #NO_TUPLE}.
         */
        private Map<Object, Object>[] maps;
        /**
         * Deferred classes only.
         */
        private final Map<Object, FactHandle> handleMap;
        /**
         * Deferred classes only; inactive nodes keep their position, because handles address nodes by position.
         */
        private final boolean[] active;
        private AbstractPrecomputeNode<?>[] otherNodes;

        @SuppressWarnings("unchecked")
        private ClassPlan(Class<?> factClass, List<AbstractRootNode<?>> rootNodeList, boolean deferred) {
            this.factClass = factClass;
            var forEachNodeList = new ArrayList<AbstractForEachUniNode<Object>>();
            var otherNodeList = new ArrayList<AbstractPrecomputeNode<?>>();
            for (var node : rootNodeList) {
                switch (node) {
                    case AbstractForEachUniNode<?> forEachNode ->
                        forEachNodeList.add((AbstractForEachUniNode<Object>) forEachNode);
                    case AbstractPrecomputeNode<?> precomputeNode -> otherNodeList.add(precomputeNode);
                    default -> throw new IllegalStateException("Impossible state: unknown root node (%s)."
                            .formatted(node));
                }
            }
            this.forEachNodes = forEachNodeList.toArray(AbstractForEachUniNode[]::new);
            this.deferred = deferred && forEachNodes.length > 0;
            this.active = new boolean[forEachNodes.length];
            Arrays.fill(active, true);
            if (this.deferred) {
                this.maps = new Map[0];
                this.handleMap = HashMap.newHashMap(1000);
            } else {
                this.maps = new Map[forEachNodes.length];
                for (var i = 0; i < maps.length; i++) {
                    maps[i] = HashMap.newHashMap(1000);
                }
                this.handleMap = Map.of();
            }
            this.otherNodes = otherNodeList.toArray(AbstractPrecomputeNode[]::new);
        }

        @SuppressWarnings("unchecked")
        private void removeInactiveNodes(Predicate<Object> isActive) {
            if (deferred) { // Handles address nodes by position, so positions stay.
                for (var i = 0; i < forEachNodes.length; i++) {
                    active[i] = isActive.test(forEachNodes[i]);
                }
            } else {
                var keptNodes = new ArrayList<AbstractForEachUniNode<Object>>();
                var keptMaps = new ArrayList<Map<Object, Object>>();
                for (var i = 0; i < forEachNodes.length; i++) {
                    if (isActive.test(forEachNodes[i])) {
                        keptNodes.add(forEachNodes[i]);
                        keptMaps.add(maps[i]);
                    }
                }
                forEachNodes = keptNodes.toArray(AbstractForEachUniNode[]::new);
                maps = keptMaps.toArray(Map[]::new);
            }
            otherNodes = Arrays.stream(otherNodes)
                    .filter(isActive)
                    .toArray(AbstractPrecomputeNode[]::new);
        }

    }

    /**
     * The tuples of one fact of a deferred class, by position in {@link ClassPlan#forEachNodes}.
     */
    private static final class FactHandle {

        private final Object fact;
        private final ClassPlan plan;
        private final @Nullable UniTuple<Object>[] tuples;
        private boolean dirty = false;
        private boolean retracted = false;

        @SuppressWarnings("unchecked")
        private FactHandle(Object fact, ClassPlan plan) {
            this.fact = fact;
            this.plan = plan;
            this.tuples = new UniTuple[plan.forEachNodes.length];
        }

    }

}
