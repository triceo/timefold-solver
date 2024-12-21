package ai.timefold.solver.core.impl.score.stream.bavet.common;

import java.util.function.Consumer;
import java.util.function.Function;

import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.AbstractTuple;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.LeftTupleLifecycle;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.RightTupleLifecycle;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.TupleLifecycle;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.TupleState;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.UniTuple;
import ai.timefold.solver.core.impl.util.ElementAwareList;
import ai.timefold.solver.core.impl.util.ElementAwareListEntry;

/**
 * This class has two direct children: {@link AbstractIndexedJoinNode} and {@link AbstractUnindexedJoinNode}.
 * The logic in either is identical, except that the latter removes all indexing work.
 * Therefore any time that one of the classes changes,
 * the other should be inspected if it could benefit from applying the change there too.
 *
 * @param <LeftTuple_>
 * @param <Right_>
 */
public abstract class AbstractJoinNode<LeftTuple_ extends AbstractTuple, Right_, OutTuple_ extends AbstractTuple>
        extends AbstractNode
        implements LeftTupleLifecycle<LeftTuple_>, RightTupleLifecycle<UniTuple<Right_>> {

    protected final int inputStoreIndexLeft;
    protected final int inputStoreIndexRight;
    private final boolean isFiltering;
    protected final int outputStoreSize;
    private final int outputStoreIndex;
    private final StaticPropagationQueue<OutTuple_> propagationQueue;

    protected AbstractJoinNode(int inputStoreIndexLeft, int inputStoreIndexRight,
            TupleLifecycle<OutTuple_> nextNodesTupleLifecycle, boolean isFiltering, int outputStoreSize) {
        this.inputStoreIndexLeft = inputStoreIndexLeft;
        this.inputStoreIndexRight = inputStoreIndexRight;
        this.isFiltering = isFiltering;
        this.outputStoreSize = outputStoreSize;
        this.outputStoreIndex = outputStoreSize - 1;
        this.propagationQueue = new StaticPropagationQueue<>(nextNodesTupleLifecycle);
    }

    protected abstract OutTuple_ createOutTuple(LeftTuple_ leftTuple, UniTuple<Right_> rightTuple);

    protected abstract void setOutTupleLeftFacts(OutTuple_ outTuple, LeftTuple_ leftTuple);

    protected abstract void setOutTupleRightFact(OutTuple_ outTuple, UniTuple<Right_> rightTuple);

    protected abstract boolean testFiltering(LeftTuple_ leftTuple, UniTuple<Right_> rightTuple);

    protected final void insertOutTuple(LeftTuple_ leftTuple, ElementAwareList<OutTuple_> outTupleListLeft,
            UniTuple<Right_> rightTuple, ElementAwareList<OutTuple_> outTupleListRight) {
        var outTuple = createOutTuple(leftTuple, rightTuple);
        var outJoinStore = new OutJoinStore<OutTuple_>();
        outJoinStore.leftOutListEntry = outTupleListLeft.add(outTuple);
        outJoinStore.rightOutListEntry = outTupleListRight.add(outTuple);
        outTuple.setStore(outputStoreIndex, outJoinStore);
        propagationQueue.insert(outTuple);
    }

    protected final void insertOutTupleFiltered(LeftTuple_ leftTuple, AbstractJoinStore<OutTuple_> leftStore,
            UniTuple<Right_> rightTuple, AbstractJoinStore<OutTuple_> rightStore) {
        if (!isFiltering || testFiltering(leftTuple, rightTuple)) {
            insertOutTuple(leftTuple, leftStore.outList, rightTuple, rightStore.outList);
        }
    }

    protected final void innerUpdateLeft(LeftTuple_ leftTuple, AbstractJoinStore<OutTuple_> leftStore,
            Consumer<Consumer<UniTuple<Right_>>> rightTupleConsumer) {
        // Prefer an update over retract-insert if possible
        if (!isFiltering) {
            // Propagate the update for downstream filters, matchWeighers, ...
            for (var outTuple : leftStore.outList) {
                updateOutTupleLeft(outTuple, leftTuple);
            }
        } else {
            rightTupleConsumer.accept(rightTuple -> {
                AbstractJoinStore<OutTuple_> rightStore = rightTuple.getStore(inputStoreIndexRight);
                processOutTupleUpdate(leftTuple, leftStore.outList, rightTuple, rightStore.outList, rightStore.outList,
                        leftStore.outList, outStore -> outStore.rightOutListEntry);
            });
        }
    }

    private void updateOutTupleLeft(OutTuple_ outTuple, LeftTuple_ leftTuple) {
        setOutTupleLeftFacts(outTuple, leftTuple);
        doUpdateOutTuple(outTuple);
    }

    private void doUpdateOutTuple(OutTuple_ outTuple) {
        var state = outTuple.state;
        if (!state.isActive()) { // Impossible because they shouldn't linger in the indexes.
            throw new IllegalStateException("Impossible state: The tuple (" + outTuple.state + ") in node (" +
                    this + ") is in an unexpected state (" + outTuple.state + ").");
        } else if (state != TupleState.OK) { // Already in the queue in the correct state.
            return;
        }
        propagationQueue.update(outTuple);
    }

    protected final void innerUpdateRight(UniTuple<Right_> rightTuple, AbstractJoinStore<OutTuple_> rightStore,
            Consumer<Consumer<LeftTuple_>> leftTupleConsumer) {
        // Prefer an update over retract-insert if possible
        if (!isFiltering) {
            // Propagate the update for downstream filters, matchWeighers, ...
            for (var outTuple : rightStore.outList) {
                setOutTupleRightFact(outTuple, rightTuple);
                doUpdateOutTuple(outTuple);
            }
        } else {
            leftTupleConsumer.accept(leftTuple -> {
                AbstractJoinStore<OutTuple_> leftStore = leftTuple.getStore(inputStoreIndexLeft);
                processOutTupleUpdate(leftTuple, leftStore.outList, rightTuple, rightStore.outList, leftStore.outList,
                        rightStore.outList, outStore -> outStore.leftOutListEntry);
            });
        }
    }

    private void processOutTupleUpdate(LeftTuple_ leftTuple, ElementAwareList<OutTuple_> outTupleListLeft,
            UniTuple<Right_> rightTuple, ElementAwareList<OutTuple_> outTupleListRight, ElementAwareList<OutTuple_> outList,
            ElementAwareList<OutTuple_> outTupleList,
            Function<OutJoinStore<OutTuple_>, ElementAwareListEntry<OutTuple_>> outEntryFunction) {
        var outTuple = findOutTuple(outTupleList, outList, outEntryFunction);
        if (testFiltering(leftTuple, rightTuple)) {
            if (outTuple == null) {
                insertOutTuple(leftTuple, outTupleListLeft, rightTuple, outTupleListRight);
            } else {
                updateOutTupleLeft(outTuple, leftTuple);
            }
        } else {
            if (outTuple != null) {
                retractOutTuple(outTuple);
            }
        }
    }

    private OutTuple_ findOutTuple(ElementAwareList<OutTuple_> outTupleList, ElementAwareList<OutTuple_> outList,
            Function<OutJoinStore<OutTuple_>, ElementAwareListEntry<OutTuple_>> outEntryFunction) {
        // Hack: the outTuple has no left/right input tuple reference, use the left/right outList reference instead.
        var item = outTupleList.first();
        while (item != null) {
            // Creating list iterators here caused major GC pressure; therefore, we iterate over the entries directly.
            var outTuple = item.getElement();
            OutJoinStore<OutTuple_> outStore = outTuple.getStore(outputStoreIndex);
            var outEntry = outEntryFunction.apply(outStore);
            var outEntryList = outEntry.getList();
            if (outList == outEntryList) {
                return outTuple;
            }
            item = item.next();
        }
        return null;
    }

    protected final void retractOutTuple(OutTuple_ outTuple) {
        OutJoinStore<OutTuple_> outStore = outTuple.removeStore(outputStoreIndex);
        outStore.leftOutListEntry.remove();
        outStore.rightOutListEntry.remove();
        var state = outTuple.state;
        if (!state.isActive()) {
            // Impossible because they shouldn't linger in the indexes.
            throw new IllegalStateException("Impossible state: The tuple (" + outTuple.state + ") in node (" + this
                    + ") is in an unexpected state (" + outTuple.state + ").");
        }
        propagationQueue.retract(outTuple, state == TupleState.CREATING ? TupleState.ABORTING : TupleState.DYING);
    }

    @Override
    public Propagator getPropagator() {
        return propagationQueue;
    }

    protected static sealed abstract class AbstractJoinStore<OutTuple_ extends AbstractTuple>
            permits AbstractIndexedJoinNode.IndexedJoinStore, AbstractUnindexedJoinNode.UnindexedJoinStore {

        protected ElementAwareList<OutTuple_> outList;

    }

    private static final class OutJoinStore<OutTuple_ extends AbstractTuple> {

        private ElementAwareListEntry<OutTuple_> leftOutListEntry;
        private ElementAwareListEntry<OutTuple_> rightOutListEntry;

    }

}
