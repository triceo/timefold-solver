package ai.timefold.solver.core.impl.bavet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * Updates of facts with for-each nodes are deferred to {@link #settle()},
 * so that each fact reaches each for-each node at most once per settle,
 * no matter how many times it was updated.
 * Each such fact has one {@link FactHandle}, which holds its tuple in every for-each node,
 * so that one map lookup serves all for-each nodes.
 */
@NullMarked
public abstract class AbstractSession<Network_ extends AbstractBavetNodeNetwork> {

    protected final Network_ nodeNetwork;
    private final Map<Class<?>, ClassPlan> classPlanMap;
    private final List<FactHandle> dirtyHandleList = new ArrayList<>();
    private @Nullable Class<?> lastClass;
    private @Nullable ClassPlan lastClassPlan;
    /**
     * Consecutive updates of one fact need no lookup.
     * Must be reset whenever an update could otherwise be lost: on settle, insert and retract.
     */
    private @Nullable Object lastUpdatedFact;
    private boolean initialized = false;
    private boolean settled = false;

    protected AbstractSession(Network_ nodeNetwork) {
        this.nodeNetwork = nodeNetwork;
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
        var handle = plan.handleMap.get(fact);
        if (handle == null) {
            throw new IllegalStateException("The fact (%s) was never inserted."
                    .formatted(fact));
        }
        lastUpdatedFact = fact;
        if (!handle.dirty) {
            handle.dirty = true;
            dirtyHandleList.add(handle);
        }
    }

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
                plan = new ClassPlan(nodeNetwork.getRootNodesAcceptingType(factClass).toList());
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
            if (handle.retracted) {
                continue;
            }
            var plan = handle.plan;
            var forEachNodes = plan.forEachNodes;
            var tuples = handle.tuples;
            for (var i = 0; i < forEachNodes.length; i++) {
                if (plan.forEachActive[i]) {
                    tuples[i] = forEachNodes[i].updateFact(handle.fact, tuples[i]);
                }
            }
        }
        dirtyHandleList.clear();
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
        private AbstractPrecomputeNode<?>[] otherNodes;

        @SuppressWarnings("unchecked")
        private ClassPlan(List<AbstractRootNode<?>> rootNodeList) {
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
            this.handleMap = forEachNodes.length == 0 ? Map.of() : new HashMap<>();
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
