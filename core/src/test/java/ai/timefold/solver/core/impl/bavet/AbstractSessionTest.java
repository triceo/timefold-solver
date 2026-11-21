package ai.timefold.solver.core.impl.bavet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import ai.timefold.solver.core.impl.bavet.common.AbstractNode;
import ai.timefold.solver.core.impl.bavet.common.AbstractRootNode;
import ai.timefold.solver.core.impl.bavet.common.tuple.TupleLifecycle;
import ai.timefold.solver.core.impl.bavet.common.tuple.UniTuple;
import ai.timefold.solver.core.impl.bavet.uni.AbstractForEachUniNode;
import ai.timefold.solver.core.impl.bavet.uni.ForEachFilteredUniNode;
import ai.timefold.solver.core.impl.bavet.uni.ForEachUnfilteredUniNode;

import org.junit.jupiter.api.Test;

/**
 * Covers the immediate update path of {@link AbstractSession} (one fact map per node)
 * and the deferred path (one handle per fact).
 */
class AbstractSessionTest {

    private final AtomicInteger filterCallCount = new AtomicInteger();
    private final TupleLifecycle<UniTuple<Fact>> filteredDownstream = mockDownstream();
    private final TupleLifecycle<UniTuple<Fact>> unfilteredDownstream = mockDownstream();

    @Test
    void immediatePathFollowsFilterTransitions() {
        var session = session(false, filteredNode());
        var fact = new Fact("A", true);
        session.insert(fact);
        session.settle();
        verify(filteredDownstream, times(1)).insert(any());

        fact.passing = false;
        session.update(fact);
        session.settle();
        verify(filteredDownstream, times(1)).retract(any());

        fact.passing = true;
        session.update(fact);
        session.settle();
        verify(filteredDownstream, times(2)).insert(any());
    }

    @Test
    void immediatePathAcceptsEqualInstanceOnUpdate() {
        var session = session(false, filteredNode());
        session.insert(new Fact("A", true));
        session.settle();

        session.update(new Fact("A", true));
        session.settle();
        verify(filteredDownstream, times(1)).update(any());
    }

    @Test
    void immediatePathRejectsDuplicateAndUnknownFacts() {
        var session = session(false, filteredNode());
        session.insert(new Fact("A", true));

        assertThatThrownBy(() -> session.insert(new Fact("A", true)))
                .hasMessageContaining("was already inserted");
        assertThatThrownBy(() -> session.update(new Fact("B", true)))
                .hasMessageContaining("was never inserted");
        assertThatThrownBy(() -> session.retract(new Fact("B", true)))
                .hasMessageContaining("was never inserted");
    }

    @Test
    void immediatePathWithSeveralNodesUpdatesAtOnce() {
        var session = session(false, filteredNode(), unfilteredNode());
        var fact = new Fact("A", true);
        session.insert(fact);
        session.settle();
        filterCallCount.set(0);

        session.update(fact);
        session.update(fact);
        assertThat(filterCallCount).hasValue(2);
        session.settle();
        verify(filteredDownstream, times(1)).update(any());
        verify(unfilteredDownstream, times(1)).update(any());
    }

    @Test
    void deferredPathUpdatesEachNodeOncePerSettle() {
        var session = session(true, filteredNode(), unfilteredNode());
        var fact = new Fact("A", true);
        var otherFact = new Fact("B", true);
        session.insert(fact);
        session.insert(otherFact);
        session.settle();
        filterCallCount.set(0);

        session.update(fact);
        session.update(fact);
        session.update(otherFact);
        session.update(fact);
        assertThat(filterCallCount).hasValue(0);
        session.settle();
        assertThat(filterCallCount).hasValue(2);
        verify(filteredDownstream, times(2)).update(any());
        verify(unfilteredDownstream, times(2)).update(any());
    }

    @Test
    void deferredPathPropagatesSameFactAgainAfterSettle() {
        // A move notifies the fact last, its undo notifies the same fact first.
        var session = session(true, filteredNode(), unfilteredNode());
        var fact = new Fact("A", true);
        session.insert(fact);
        session.settle();

        session.update(fact);
        session.settle();
        session.update(fact);
        session.settle();
        verify(unfilteredDownstream, times(2)).update(any());
    }

    @Test
    void deferredPathSkipsUpdateOfRetractedFact() {
        var session = session(true, filteredNode(), unfilteredNode());
        var fact = new Fact("A", true);
        session.insert(fact);
        session.settle();

        session.update(fact);
        session.retract(fact);
        session.settle();
        verify(unfilteredDownstream, times(0)).update(any());
        verify(unfilteredDownstream, times(1)).retract(any());
    }

    @SuppressWarnings("unchecked")
    private static TupleLifecycle<UniTuple<Fact>> mockDownstream() {
        TupleLifecycle<UniTuple<Fact>> downstream = mock(TupleLifecycle.class);
        when(downstream.isActive()).thenReturn(true);
        return downstream;
    }

    private ForEachFilteredUniNode<Fact> filteredNode() {
        return new ForEachFilteredUniNode<>(Fact.class, fact -> {
            filterCallCount.incrementAndGet();
            return fact.passing;
        }, filteredDownstream, false, 1);
    }

    private ForEachUnfilteredUniNode<Fact> unfilteredNode() {
        return new ForEachUnfilteredUniNode<>(Fact.class, unfilteredDownstream, false, 1);
    }

    @SafeVarargs
    private static TestSession session(boolean deferUpdates, AbstractForEachUniNode<Fact>... nodes) {
        for (var node : nodes) {
            node.setLayerIndex(0);
        }
        var network = new TestNetwork(List.of(nodes));
        return new TestSession(network, deferUpdates ? Set.of(Fact.class) : Set.of());
    }

    private static final class TestNetwork extends AbstractBavetNodeNetwork {

        private TestNetwork(List<AbstractForEachUniNode<Fact>> nodeList) {
            super(Map.of(Fact.class, List.<AbstractRootNode<?>> copyOf(nodeList)),
                    new AbstractNode[][] { nodeList.toArray(AbstractNode[]::new) }, AbstractNode::getPropagator);
        }

    }

    private static final class TestSession extends AbstractSession<TestNetwork> {

        private TestSession(TestNetwork network, Set<Class<?>> deferredUpdateClassSet) {
            super(network, deferredUpdateClassSet);
        }

    }

    private static final class Fact {

        private final String id;
        private boolean passing;

        private Fact(String id, boolean passing) {
            this.id = id;
            this.passing = passing;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Fact other && id.equals(other.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

    }

}
