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
 * should also be made to {@link AbstractIndexedIfExistsNode}.
 *
 * @param <LeftTuple_>
 * @param <Right_>
 */
public abstract class AbstractUnindexedIfExistsNode<LeftTuple_ extends AbstractTuple, Right_>
        extends AbstractIfExistsNode<LeftTuple_, Right_>
        implements LeftTupleLifecycle<LeftTuple_>, RightTupleLifecycle<UniTuple<Right_>> {

    // Acts as a leftTupleList too
    private final ElementAwareList<ExistsCounter<LeftTuple_>> leftCounterList = new ElementAwareList<>();
    private final ElementAwareList<UniTuple<Right_>> rightTupleList = new ElementAwareList<>();

    protected AbstractUnindexedIfExistsNode(boolean shouldExist,
            int inputStoreIndexLeft, int inputStoreIndexRight,
            TupleLifecycle<LeftTuple_> nextNodesTupleLifecycle, boolean isFiltering) {
        super(shouldExist, inputStoreIndexLeft, inputStoreIndexRight, nextNodesTupleLifecycle,
                isFiltering);
    }

    @Override
    public final void insertLeft(LeftTuple_ leftTuple) {
        LeftUnindexedIfExistsStore<LeftTuple_> leftStore =
                leftTuple.getStore(inputStoreIndexLeft, LeftUnindexedIfExistsStore::new);
        if (leftStore.entry != null) {
            throw new IllegalStateException("Impossible state: the input for the tuple (" + leftTuple
                    + ") was already added in the tupleStore.");
        }
        var counter = new ExistsCounter<>(leftTuple);
        leftStore.entry = leftCounterList.add(counter);

        if (!isFiltering) {
            counter.countRight = rightTupleList.size();
        } else {
            var leftTrackerList = new ElementAwareList<FilteringTracker<LeftTuple_>>();
            for (var rightTuple : rightTupleList) {
                RightUnindexedIfExistsStore<LeftTuple_, Right_> rightStore = rightTuple.getStore(inputStoreIndexRight);
                updateCounterFromLeft(leftTuple, rightTuple, rightStore, counter, leftTrackerList);
            }
            leftStore.trackerList = leftTrackerList;
        }
        initCounterLeft(counter);
    }

    @Override
    public final void updateLeft(LeftTuple_ leftTuple) {
        LeftUnindexedIfExistsStore<LeftTuple_> leftStore =
                leftTuple.getStore(inputStoreIndexLeft, LeftUnindexedIfExistsStore::new);
        if (leftStore.entry == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            insertLeft(leftTuple);
            return;
        }
        var counter = leftStore.entry.getElement();
        // The indexers contain counters in the DEAD state, to track the rightCount.
        if (!isFiltering) {
            updateUnchangedCounterLeft(counter);
        } else {
            // Call filtering for the leftTuple and rightTuple combinations again
            leftStore.trackerList.forEach(FilteringTracker::remove);
            counter.countRight = 0;
            for (var rightTuple : rightTupleList) {
                RightUnindexedIfExistsStore<LeftTuple_, Right_> rightStore = rightTuple.getStore(inputStoreIndexRight);
                updateCounterFromLeft(leftTuple, rightTuple, rightStore, counter, leftStore.trackerList);
            }
            updateCounterLeft(counter);
        }
    }

    @Override
    public final void retractLeft(LeftTuple_ leftTuple) {
        LeftUnindexedIfExistsStore<LeftTuple_> leftStore = leftTuple.removeStore(inputStoreIndexLeft);
        var counterEntry = leftStore.entry;
        if (counterEntry == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            return;
        }
        var counter = counterEntry.getElement();
        counterEntry.remove();
        if (isFiltering) {
            leftStore.trackerList.forEach(FilteringTracker::remove);
        }
        killCounterLeft(counter);
    }

    @Override
    public final void insertRight(UniTuple<Right_> rightTuple) {
        RightUnindexedIfExistsStore<LeftTuple_, Right_> rightStore =
                rightTuple.getStore(inputStoreIndexRight, RightUnindexedIfExistsStore::new);
        if (rightStore.entry != null) {
            throw new IllegalStateException("Impossible state: the input for the tuple (" + rightTuple
                    + ") was already added in the tupleStore.");
        }
        rightStore.entry = rightTupleList.add(rightTuple);
        if (!isFiltering) {
            leftCounterList.forEach(this::incrementCounterRight);
        } else {
            var rightTrackerList = new ElementAwareList<FilteringTracker<LeftTuple_>>();
            for (var tuple : leftCounterList) {
                updateCounterFromRight(rightTuple, tuple, rightTrackerList);
            }
            rightStore.trackerList = rightTrackerList;
        }
    }

    @Override
    public final void updateRight(UniTuple<Right_> rightTuple) {
        RightUnindexedIfExistsStore<LeftTuple_, Right_> rightStore =
                rightTuple.getStore(inputStoreIndexRight, RightUnindexedIfExistsStore::new);
        var rightEntry = rightStore.entry;
        if (rightEntry == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            insertRight(rightTuple);
            return;
        }
        if (isFiltering) {
            var rightTrackerList = updateRightTrackerList(rightStore);
            for (var tuple : leftCounterList) {
                updateCounterFromRight(rightTuple, tuple, rightTrackerList);
            }
        }
    }

    @Override
    public final void retractRight(UniTuple<Right_> rightTuple) {
        RightUnindexedIfExistsStore<LeftTuple_, Right_> rightStore = rightTuple.removeStore(inputStoreIndexRight);
        var rightEntry = rightStore.entry;
        if (rightEntry == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            return;
        }
        rightEntry.remove();
        if (!isFiltering) {
            leftCounterList.forEach(this::decrementCounterRight);
        } else {
            updateRightTrackerList(rightStore);
        }
    }

    static final class LeftUnindexedIfExistsStore<Tuple_ extends AbstractTuple> extends AbstractIfExistsStore<Tuple_> {

        private ElementAwareListEntry<ExistsCounter<Tuple_>> entry;

    }

    static final class RightUnindexedIfExistsStore<LeftTuple_ extends AbstractTuple, Right_>
            extends AbstractIfExistsStore<LeftTuple_> {

        private ElementAwareListEntry<UniTuple<Right_>> entry;

    }

}
