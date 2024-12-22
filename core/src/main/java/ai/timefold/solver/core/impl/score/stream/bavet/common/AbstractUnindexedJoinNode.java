package ai.timefold.solver.core.impl.score.stream.bavet.common;

import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.AbstractTuple;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.LeftTupleLifecycle;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.RightTupleLifecycle;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.TupleLifecycle;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.UniTuple;
import ai.timefold.solver.core.impl.util.ElementAwareList;
import ai.timefold.solver.core.impl.util.ElementAwareListEntry;

/**
 * There is a strong likelihood that any change made to this class
 * should also be made to {@link AbstractIndexedJoinNode}.
 *
 * @param <LeftTuple_>
 * @param <Right_>
 */
public abstract class AbstractUnindexedJoinNode<LeftTuple_ extends AbstractTuple, Right_, OutTuple_ extends AbstractTuple>
        extends AbstractJoinNode<LeftTuple_, Right_, OutTuple_>
        implements LeftTupleLifecycle<LeftTuple_>, RightTupleLifecycle<UniTuple<Right_>> {

    private final ElementAwareList<LeftTuple_> leftTupleList = new ElementAwareList<>();
    private final ElementAwareList<UniTuple<Right_>> rightTupleList = new ElementAwareList<>();

    protected AbstractUnindexedJoinNode(int inputStoreIndexLeftOutTupleList, int inputStoreIndexRightOutTupleList,
            TupleLifecycle<OutTuple_> nextNodesTupleLifecycle, boolean isFiltering, int outputStoreSize) {
        super(inputStoreIndexLeftOutTupleList, inputStoreIndexRightOutTupleList, nextNodesTupleLifecycle, isFiltering,
                outputStoreSize);
    }

    @Override
    public final void insertLeft(LeftTuple_ leftTuple) {
        UnindexedJoinStore<LeftTuple_, OutTuple_> leftStore =
                leftTuple.getStore(inputStoreIndexLeft, UnindexedJoinStore::new);
        if (leftStore.entry != null) {
            throw new IllegalStateException("Impossible state: the input for the tuple (" + leftTuple
                    + ") was already added in the tupleStore.");
        }
        leftStore.entry = leftTupleList.add(leftTuple);
        leftStore.outList = new ElementAwareList<>();
        for (var rightTuple : rightTupleList) {
            UnindexedJoinStore<UniTuple<Right_>, OutTuple_> rightStore = rightTuple.getStore(inputStoreIndexRight);
            insertOutTupleFiltered(leftTuple, leftStore.outList, rightTuple, rightStore.outList);
        }
    }

    @Override
    public final void updateLeft(LeftTuple_ leftTuple) {
        UnindexedJoinStore<LeftTuple_, OutTuple_> leftStore = leftTuple.getStore(inputStoreIndexLeft);
        if (leftStore == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            insertLeft(leftTuple);
            return;
        }
        innerUpdateLeft(leftTuple, leftStore, rightTupleList::forEach);
    }

    @Override
    public final void retractLeft(LeftTuple_ leftTuple) {
        UnindexedJoinStore<LeftTuple_, OutTuple_> leftStore = leftTuple.removeStore(inputStoreIndexLeft);
        if (leftStore == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            return;
        }
        leftStore.entry.remove();
        leftStore.outList.forEach(this::retractOutTuple);
    }

    @Override
    public final void insertRight(UniTuple<Right_> rightTuple) {
        UnindexedJoinStore<UniTuple<Right_>, OutTuple_> rightStore =
                rightTuple.getStore(inputStoreIndexRight, UnindexedJoinStore::new);
        if (rightStore.entry != null) {
            throw new IllegalStateException("Impossible state: the input for the tuple (" + rightTuple
                    + ") was already added in the tupleStore.");
        }
        rightStore.entry = rightTupleList.add(rightTuple);
        rightStore.outList = new ElementAwareList<>();
        rightTuple.setStore(inputStoreIndexRight, rightStore);
        for (LeftTuple_ tuple : leftTupleList) {
            UnindexedJoinStore<LeftTuple_, OutTuple_> leftStore = tuple.getStore(inputStoreIndexLeft);
            insertOutTupleFiltered(tuple, leftStore.outList, rightTuple, rightStore.outList);
        }
    }

    @Override
    public final void updateRight(UniTuple<Right_> rightTuple) {
        UnindexedJoinStore<UniTuple<Right_>, OutTuple_> rightStore = rightTuple.getStore(inputStoreIndexRight);
        if (rightStore == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            insertRight(rightTuple);
            return;
        }
        innerUpdateRight(rightTuple, rightStore, leftTupleList::forEach);
    }

    @Override
    public final void retractRight(UniTuple<Right_> rightTuple) {
        UnindexedJoinStore<UniTuple<Right_>, OutTuple_> rightStore = rightTuple.removeStore(inputStoreIndexRight);
        if (rightStore == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            return;
        }
        rightStore.entry.remove();
        rightStore.outList.forEach(this::retractOutTuple);
    }

    static final class UnindexedJoinStore<Tuple_ extends AbstractTuple, OutTuple_ extends AbstractTuple>
            extends AbstractJoinStore<OutTuple_> {

        private ElementAwareListEntry<Tuple_> entry;

    }

}
