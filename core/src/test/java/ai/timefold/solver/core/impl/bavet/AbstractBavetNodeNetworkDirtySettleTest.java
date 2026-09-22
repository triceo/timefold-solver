package ai.timefold.solver.core.impl.bavet;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintRef;
import ai.timefold.solver.core.impl.bavet.common.AbstractNode;
import ai.timefold.solver.core.impl.bavet.common.AbstractRootNode;
import ai.timefold.solver.core.impl.bavet.common.BavetStream;
import ai.timefold.solver.core.impl.bavet.common.ConstraintNodeProfileId;
import ai.timefold.solver.core.impl.bavet.common.InnerConstraintProfiler;
import ai.timefold.solver.core.impl.bavet.common.ProfilingPropagator;
import ai.timefold.solver.core.impl.bavet.common.Propagator;
import ai.timefold.solver.core.impl.bavet.common.StreamKind;
import ai.timefold.solver.core.impl.bavet.common.tuple.TupleLifecycle;
import ai.timefold.solver.core.impl.bavet.common.tuple.UniTuple;
import ai.timefold.solver.core.impl.bavet.uni.ForEachUnfilteredUniNode;

import org.junit.jupiter.api.Test;

class AbstractBavetNodeNetworkDirtySettleTest {

    @Test
    void settleSkipsNodesWithoutPendingWork() {
        var nodeA = new ForEachUnfilteredUniNode<>(String.class, new NoopTupleLifecycle<>(), 0);
        var nodeB = new ForEachUnfilteredUniNode<>(Integer.class, new NoopTupleLifecycle<>(), 0);
        nodeA.setId(0);
        nodeB.setId(1);
        nodeA.setLayerIndex(0);
        nodeB.setLayerIndex(0);
        var profileIdA = new ConstraintNodeProfileId(0, StreamKind.FOR_EACH, new TreeSet<>());
        var profileIdB = new ConstraintNodeProfileId(1, StreamKind.FOR_EACH, new TreeSet<>());
        var profiler = new CountingProfiler();
        Function<AbstractNode, Propagator> propagatorFunction = node -> new ProfilingPropagator(profiler,
                node == nodeA ? profileIdA : profileIdB, node.getPropagator());
        var network = new TestNetwork(Map.of(String.class, List.of(nodeA), Integer.class, List.of(nodeB)),
                AbstractBavetNodeNetwork.buildLayeredNodes(List.of(nodeA, nodeB)), propagatorFunction);

        var a = "a";
        nodeA.insert(a);
        nodeB.insert(1);
        network.settle();
        assertThat(profiler.countMap)
                .containsEntry(profileIdA, 3)
                .containsEntry(profileIdB, 3);

        profiler.countMap.clear();
        nodeA.update(a);
        network.settle();
        assertThat(profiler.countMap)
                .containsEntry(profileIdA, 3)
                .doesNotContainKey(profileIdB);

        profiler.countMap.clear();
        network.settle();
        assertThat(profiler.countMap).isEmpty();
    }

    private static final class TestNetwork extends AbstractBavetNodeNetwork {

        TestNetwork(Map<Class<?>, List<AbstractRootNode<?>>> declaredClassToNodeMap, AbstractNode[][] layeredNodes,
                Function<AbstractNode, Propagator> propagatorFunction) {
            super(declaredClassToNodeMap, layeredNodes, propagatorFunction);
        }

    }

    private static final class NoopTupleLifecycle<A> implements TupleLifecycle<UniTuple<A>> {

        @Override
        public void insert(UniTuple<A> tuple) {
            // Nothing downstream.
        }

        @Override
        public void update(UniTuple<A> tuple) {
            // Nothing downstream.
        }

        @Override
        public void retract(UniTuple<A> tuple) {
            // Nothing downstream.
        }

        @Override
        public void afterAllFactsInserted(boolean upstreamCanProduceTuples) {
            // Always active.
        }

        @Override
        public boolean isActive() {
            return true;
        }

    }

    private static final class CountingProfiler implements InnerConstraintProfiler {

        private final Map<ConstraintNodeProfileId, Integer> countMap = new HashMap<>();

        @Override
        public void register(ConstraintNodeProfileId profileId) {
            // Not used.
        }

        @Override
        public <Solution_, Stream_ extends BavetStream> void registerNodeGraph(Solution_ solution,
                List<AbstractNode> nodeList, Set<Constraint> constraintSet,
                Function<AbstractNode, Stream_> nodeToStreamFunction,
                Function<Stream_, AbstractNode> streamToParentNodeFunction) {
            // Not used.
        }

        @Override
        public void registerConstraint(ConstraintRef constraintRef, Set<ConstraintNodeProfileId> profileIdSet) {
            // Not used.
        }

        @Override
        public void measure(ConstraintNodeProfileId profileId, Operation operation, Runnable measurable) {
            countMap.merge(profileId, 1, Integer::sum);
            measurable.run();
        }

        @Override
        public void summarize() {
            // Not used.
        }

    }

}
