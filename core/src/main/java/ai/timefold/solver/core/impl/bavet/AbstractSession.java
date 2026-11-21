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

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Feeds facts into the root nodes of a node network.
 * Inserts and retracts reach the nodes immediately.
 * Each fact with for-each nodes has one {@link FactHandle}, which holds its tuple in every for-each node,
 * so that one map lookup serves all for-each nodes.
 * Updates of facts of the deferred classes wait until {@link #settle()},
 * so that each fact reaches each for-each node at most once per settle;
 * these are the classes which typically receive many updates per fact between two settles,
 * such as entities with declarative shadow variables.
 * Deferral costs a second pass over the facts, so updates of other classes reach the nodes immediately.
 * A class with only one for-each node and no deferral needs no handle;
 * its map holds the tuple directly, which saves pointer loads on every update.
 */
@NullMarked
public abstract class AbstractSession<Network_ extends AbstractBavetNodeNetwork> {

    /**
     * Value of {@link ClassPlan#directMap} for a fact which its node holds no tuple for.
     */
    private static final Object NO_TUPLE = new Object();
    private static final int INITIAL_FACT_MAP_SIZE = 1000;

    protected final Network_ nodeNetwork;
    private final Set<Class<?>> deferredUpdateClassSet;
    private final Map<Class<?>, ClassPlan> classPlanMap;
    private final List<FactHandle> dirtyHandleList = new ArrayList<>();
    private @Nullable Class<?> lastClass;
    private @Nullable ClassPlan lastClassPlan;
    /**
     * The last fact of a deferred class that was updated; consecutive updates of it need no lookup.
     * Must be reset whenever an update could otherwise be lost: on settle, insert and retract.
     */
    private @Nullable Object lastUpdatedFact;
    private boolean initialized = false;
    private boolean settled = false;

    protected AbstractSession(Network_ nodeNetwork) {
        this(nodeNetwork, Set.of());
    }

    /**
     * @param deferredUpdateClassSet updates of facts of these classes and their subclasses wait until settle
     */
    protected AbstractSession(Network_ nodeNetwork, Set<Class<?>> deferredUpdateClassSet) {
        this.nodeNetwork = nodeNetwork;
        this.deferredUpdateClassSet = deferredUpdateClassSet;
        this.classPlanMap = HashMap.newHashMap(nodeNetwork.forEachNodeCount());
    }

    public final void insert(Object fact) {
        lastUpdatedFact = null;
        settled = false;
        var plan = getClassPlan(fact.getClass());
        for (var node : plan.otherNodes) {
            node.insert(fact);
        }
        if (!plan.hasForEachNodes()) {
            return;
        }
        var directNode = plan.directNode;
        if (directNode != null) {
            var tuple = plan.forEachActive[0] ? directNode.insertFact(fact) : null;
            if (plan.directMap.putIfAbsent(fact, tuple == null ? NO_TUPLE : tuple) != null) {
                throw new IllegalStateException("""
                        The fact (%s) was already inserted.
                        Maybe your entities mistakenly equal each other?
                        Maybe your @%s's collection properties contain duplicates?"""
                        .formatted(fact, PlanningSolution.class.getSimpleName()));
            }
            return;
        }
        var handle = new FactHandle(fact, plan);
        if (plan.handleMap.putIfAbsent(fact, handle) != null) {
            throw new IllegalStateException("""
                    The fact (%s) was already inserted.
                    Maybe your entities mistakenly equal each other?
                    Maybe your @%s's collection properties contain duplicates?"""
                    .formatted(fact, PlanningSolution.class.getSimpleName()));
        }
        var forEachNodes = plan.forEachNodes;
        for (var i = 0; i < forEachNodes.length; i++) {
            if (plan.forEachActive[i]) {
                handle.tuples[i] = forEachNodes[i].insertFact(fact);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public final void update(Object fact) {
        settled = false;
        if (fact == lastUpdatedFact) {
            return;
        }
        var plan = getClassPlan(fact.getClass());
        for (var node : plan.otherNodes) {
            node.update(fact);
        }
        if (!plan.hasForEachNodes()) {
            return;
        }
        var directNode = plan.directNode;
        if (directNode != null) {
            var stored = plan.directMap.get(fact);
            if (stored == null) {
                throw new IllegalStateException("The fact (%s) was never inserted."
                        .formatted(fact));
            }
            if (plan.forEachActive[0]) {
                var tuple = stored == NO_TUPLE ? null : (UniTuple<Object>) stored;
                var updated = directNode.updateFact(fact, tuple);
                if (updated != tuple) {
                    plan.directMap.put(fact, updated == null ? NO_TUPLE : updated);
                }
            }
            return;
        }
        var handle = plan.handleMap.get(fact);
        if (handle == null) {
            throw new IllegalStateException("The fact (%s) was never inserted."
                    .formatted(fact));
        }
        if (!plan.deferUpdates) {
            updateForEachNodes(handle);
            return;
        }
        lastUpdatedFact = fact;
        if (!handle.dirty) {
            handle.dirty = true;
            dirtyHandleList.add(handle);
        }
    }

    @SuppressWarnings("unchecked")
    public final void retract(Object fact) {
        lastUpdatedFact = null;
        settled = false;
        var plan = getClassPlan(fact.getClass());
        for (var node : plan.otherNodes) {
            node.retract(fact);
        }
        if (!plan.hasForEachNodes()) {
            return;
        }
        var directNode = plan.directNode;
        if (directNode != null) {
            var stored = plan.directMap.remove(fact);
            if (stored == null) {
                throw new IllegalStateException("The fact (%s) was never inserted."
                        .formatted(fact));
            }
            if (plan.forEachActive[0]) {
                directNode.retractFact(fact, stored == NO_TUPLE ? null : (UniTuple<Object>) stored);
            }
            return;
        }
        var handle = plan.handleMap.remove(fact);
        if (handle == null) {
            throw new IllegalStateException("The fact (%s) was never inserted."
                    .formatted(fact));
        }
        handle.retracted = true; // A pending update of this handle is skipped.
        var forEachNodes = plan.forEachNodes;
        for (var i = 0; i < forEachNodes.length; i++) {
            if (plan.forEachActive[i]) {
                forEachNodes[i].retractFact(fact, handle.tuples[i]);
            }
        }
    }

    private ClassPlan getClassPlan(Class<?> factClass) {
        if (factClass != lastClass) {
            // Map.computeIfAbsent() would have created lambdas on the hot path, this will not.
            var plan = classPlanMap.get(factClass);
            if (plan == null) {
                var deferUpdates = deferredUpdateClassSet.stream().anyMatch(clazz -> clazz.isAssignableFrom(factClass));
                plan = new ClassPlan(nodeNetwork.getRootNodesAcceptingType(factClass).toList(), deferUpdates);
                classPlanMap.put(factClass, plan);
            }
            lastClassPlan = plan;
            lastClass = factClass;
        }
        return lastClassPlan;
    }

    public final void settle() {
        if (settled) {
            return;
        }
        flushUpdates();
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
        lastUpdatedFact = null;
        for (var handleIndex = 0; handleIndex < dirtyHandleList.size(); handleIndex++) {
            var handle = dirtyHandleList.get(handleIndex);
            handle.dirty = false;
            if (!handle.retracted) {
                updateForEachNodes(handle);
            }
        }
        dirtyHandleList.clear();
    }

    private static void updateForEachNodes(FactHandle handle) {
        var plan = handle.plan;
        var forEachNodes = plan.forEachNodes;
        var tuples = handle.tuples;
        for (var i = 0; i < forEachNodes.length; i++) {
            if (plan.forEachActive[i]) {
                tuples[i] = forEachNodes[i].updateFact(handle.fact, tuples[i]);
            }
        }
    }

    public Network_ getNodeNetwork() {
        return nodeNetwork;
    }

    /**
     * The root nodes of one concrete fact class, and the handles of its facts.
     * Handles are kept for each concrete class,
     * so that equal facts of unrelated classes do not collide.
     */
    private static final class ClassPlan {

        private final Map<Object, FactHandle> handleMap;
        /**
         * Positions never change, because {@link FactHandle#tuples} uses them.
         */
        private final AbstractForEachUniNode<Object>[] forEachNodes;
        private final boolean[] forEachActive;
        private final boolean deferUpdates;
        /**
         * Set for a class with one for-each node and no deferral; then {@link #directMap} holds its tuples.
         */
        private final @Nullable AbstractForEachUniNode<Object> directNode;
        private final Map<Object, Object> directMap;
        private AbstractPrecomputeNode<?>[] otherNodes;

        @SuppressWarnings("unchecked")
        private ClassPlan(List<AbstractRootNode<?>> rootNodeList, boolean deferUpdates) {
            this.deferUpdates = deferUpdates;
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
            this.forEachActive = new boolean[forEachNodes.length];
            Arrays.fill(forEachActive, true);
            this.otherNodes = otherNodeList.toArray(AbstractPrecomputeNode[]::new);
            this.directNode = !deferUpdates && forEachNodes.length == 1 ? forEachNodes[0] : null;
            // A sparse table measurably speeds up lookups in small maps which are hit on every move.
            this.directMap = directNode == null ? Map.of() : HashMap.newHashMap(INITIAL_FACT_MAP_SIZE);
            this.handleMap = forEachNodes.length == 0 || directNode != null ? Map.of()
                    : HashMap.newHashMap(INITIAL_FACT_MAP_SIZE);
        }

        private boolean hasForEachNodes() {
            return forEachNodes.length != 0;
        }

        private void removeInactiveNodes(Predicate<Object> isActive) {
            for (var i = 0; i < forEachNodes.length; i++) {
                forEachActive[i] = forEachActive[i] && isActive.test(forEachNodes[i]);
            }
            otherNodes = Arrays.stream(otherNodes)
                    .filter(isActive)
                    .toArray(AbstractPrecomputeNode[]::new);
        }

    }

    private static final class FactHandle {

        private final Object fact;
        private final ClassPlan plan;
        /**
         * Index i belongs to {@link ClassPlan#forEachNodes}[i]; null if that node holds no tuple for the fact.
         */
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
