package ai.timefold.solver.core.impl.bavet.common;

import ai.timefold.solver.core.impl.bavet.common.index.IndexerFactory;
import ai.timefold.solver.core.impl.bavet.common.index.IndexerFactory.KeysExtractor;
import ai.timefold.solver.core.impl.bavet.common.index.IndexerFactory.UniKeysExtractor;
import ai.timefold.solver.core.impl.bavet.common.index.JoinIndex;
import ai.timefold.solver.core.impl.bavet.common.tuple.InOutTupleStorePositionTracker;
import ai.timefold.solver.core.impl.bavet.common.tuple.LeftTupleLifecycle;
import ai.timefold.solver.core.impl.bavet.common.tuple.RightTupleLifecycle;
import ai.timefold.solver.core.impl.bavet.common.tuple.Tuple;
import ai.timefold.solver.core.impl.bavet.common.tuple.TupleLifecycle;
import ai.timefold.solver.core.impl.bavet.common.tuple.TupleList;
import ai.timefold.solver.core.impl.bavet.common.tuple.UniTuple;
import ai.timefold.solver.core.impl.util.ListEntry;

/**
 * There is a strong likelihood that any change to this class, which is not related to indexing,
 * should also be made to {@link AbstractUnindexedJoinNode}.
 * <p>
 * Both sides are indexed in one {@link JoinIndex};
 * a tuple inserted on one side queries the other side with its own composite key.
 * The out-tuple/propagation logic is in {@link AbstractJoinNode}.
 *
 * @param <LeftTuple_>
 * @param <Right_>
 */
public abstract class AbstractIndexedJoinNode<LeftTuple_ extends Tuple, Right_, OutTuple_ extends Tuple>
        extends AbstractJoinNode<LeftTuple_, Right_, OutTuple_>
        implements LeftTupleLifecycle<LeftTuple_>, RightTupleLifecycle<UniTuple<Right_>> {

    private final KeysExtractor<LeftTuple_> keysExtractorLeft;
    private final UniKeysExtractor<Right_> keysExtractorRight;
    private final int inputStoreIndexLeftCompositeKey;
    private final int inputStoreIndexLeftEntry;
    private final int inputStoreIndexRightCompositeKey;
    private final int inputStoreIndexRightEntry;
    private final JoinIndex<LeftTuple_, UniTuple<Right_>> joinIndex;

    protected AbstractIndexedJoinNode(KeysExtractor<LeftTuple_> keysExtractorLeft, IndexerFactory<Right_> indexerFactory,
            TupleLifecycle<OutTuple_> nextNodesTupleLifecycle, boolean isFiltering,
            InOutTupleStorePositionTracker tupleStorePositionTracker) {
        super(nextNodesTupleLifecycle, isFiltering, tupleStorePositionTracker);
        this.keysExtractorLeft = keysExtractorLeft;
        this.keysExtractorRight = indexerFactory.buildRightKeysExtractor();
        this.inputStoreIndexLeftCompositeKey = tupleStorePositionTracker.reserveNextLeft();
        this.inputStoreIndexLeftEntry = tupleStorePositionTracker.reserveNextLeft();
        this.inputStoreIndexRightCompositeKey = tupleStorePositionTracker.reserveNextRight();
        this.inputStoreIndexRightEntry = tupleStorePositionTracker.reserveNextRight();
        this.joinIndex = indexerFactory.buildJoinIndex(tupleStorePositionTracker);
    }

    @Override
    public final void insertLeft(LeftTuple_ leftTuple) {
        if (leftTuple.getStore(inputStoreIndexLeftCompositeKey) != null) {
            throw new IllegalStateException(
                    "Impossible state: the input for the tuple (%s) was already added in the tupleStore."
                            .formatted(leftTuple));
        }
        var compositeKey = keysExtractorLeft.apply(leftTuple);
        leftTuple.setStore(inputStoreIndexLeftOutTupleList, leftOutTupleListBuilder.get());
        leftTuple.setStore(inputStoreIndexLeftCompositeKey, compositeKey);
        leftTuple.setStore(inputStoreIndexLeftEntry, joinIndex.putLeft(leftTuple, compositeKey, leftTuple));
        propagateLeft(leftTuple, compositeKey);
    }

    @Override
    public final void updateLeft(LeftTuple_ leftTuple) {
        var oldCompositeKey = leftTuple.getStore(inputStoreIndexLeftCompositeKey);
        if (oldCompositeKey == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            insertLeft(leftTuple);
            return;
        }
        var newCompositeKey = keysExtractorLeft.apply(leftTuple);
        if (oldCompositeKey.equals(newCompositeKey)) {
            // No need for re-indexing because the index keys didn't change
            if (isFiltering) {
                crossMatchLeft(leftTuple);
            } else {
                // Prefer an update over retract-insert if possible
                innerUpdateLeft(leftTuple,
                        consumer -> joinIndex.forEachRight(leftTuple, oldCompositeKey, consumer));
            }
        } else {
            TupleList<OutTuple_> outTupleListLeft = leftTuple.getStore(inputStoreIndexLeftOutTupleList);
            ListEntry<LeftTuple_> entry = leftTuple.getStore(inputStoreIndexLeftEntry);
            leftTuple.setStore(inputStoreIndexLeftCompositeKey, newCompositeKey);
            leftTuple.setStore(inputStoreIndexLeftEntry,
                    joinIndex.updateLeft(leftTuple, oldCompositeKey, newCompositeKey, entry));
            outTupleListLeft.clear(this::retractOutTupleLeft);
            // outTupleListLeft is now empty
            // No need for leftTuple.setStore(inputStoreIndexLeftOutTupleList, outTupleListLeft);
            propagateLeft(leftTuple, newCompositeKey);
        }
    }

    private void propagateLeft(LeftTuple_ leftTuple, Object compositeKey) {
        if (isFiltering) {
            // Defer the cross-match (the opposite-side read) to this node's own layer turn
            // instead of computing it now,
            // at whatever layer the parent that produced leftTuple happens to be in.
            // See AbstractCrossMatchNode's pendingLeft/pendingRight javadoc.
            crossMatchLeft(leftTuple);
            return;
        }
        // Non-filtering: reads the opposite side eagerly, with no per-read staleness check needed
        // (proven safe by dedicated regression tests; see AbstractJoinNode's insertOutTupleIfActiveFiltered javadoc).
        // A stale read merely produces a doomed out-tuple
        // the true retraction cleans up later via its own out-tuple list.
        joinIndex.forEachRight(leftTuple, compositeKey, rightTuple -> insertOutTupleIfActiveFiltered(leftTuple, rightTuple));
    }

    @Override
    public final void retractLeft(LeftTuple_ leftTuple) {
        clearPendingLeft(leftTuple);
        var compositeKey = leftTuple.removeStore(inputStoreIndexLeftCompositeKey);
        if (compositeKey == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            return;
        }
        TupleList<OutTuple_> outTupleListLeft = leftTuple.removeStore(inputStoreIndexLeftOutTupleList);
        ListEntry<LeftTuple_> entry = leftTuple.removeStore(inputStoreIndexLeftEntry);
        joinIndex.removeLeft(leftTuple, compositeKey, entry);
        outTupleListLeft.clear(this::retractOutTupleLeft);
    }

    @Override
    public final void insertRight(UniTuple<Right_> rightTuple) {
        if (rightTuple.getStore(inputStoreIndexRightCompositeKey) != null) {
            throw new IllegalStateException(
                    "Impossible state: the input for the tuple (%s) was already added in the tupleStore."
                            .formatted(rightTuple));
        }
        var compositeKey = keysExtractorRight.apply(rightTuple);
        rightTuple.setStore(inputStoreIndexRightOutTupleList, rightOutTupleListBuilder.get());
        rightTuple.setStore(inputStoreIndexRightCompositeKey, compositeKey);
        rightTuple.setStore(inputStoreIndexRightEntry, joinIndex.putRight(rightTuple, compositeKey, rightTuple));
        propagateRight(rightTuple, compositeKey);
    }

    @Override
    public final void updateRight(UniTuple<Right_> rightTuple) {
        var oldCompositeKey = rightTuple.getStore(inputStoreIndexRightCompositeKey);
        if (oldCompositeKey == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            insertRight(rightTuple);
            return;
        }
        var newCompositeKey = keysExtractorRight.apply(rightTuple);
        if (oldCompositeKey.equals(newCompositeKey)) {
            // No need for re-indexing because the index keys didn't change
            if (isFiltering) {
                crossMatchRight(rightTuple);
            } else {
                // Prefer an update over retract-insert if possible
                innerUpdateRight(rightTuple,
                        consumer -> joinIndex.forEachLeft(rightTuple, oldCompositeKey, consumer));
            }
        } else {
            TupleList<OutTuple_> outTupleListRight = rightTuple.getStore(inputStoreIndexRightOutTupleList);
            ListEntry<UniTuple<Right_>> entry = rightTuple.getStore(inputStoreIndexRightEntry);
            rightTuple.setStore(inputStoreIndexRightCompositeKey, newCompositeKey);
            rightTuple.setStore(inputStoreIndexRightEntry,
                    joinIndex.updateRight(rightTuple, oldCompositeKey, newCompositeKey, entry));
            outTupleListRight.clear(this::retractOutTupleRight);
            // outTupleListRight is now empty
            // No need for rightTuple.setStore(inputStoreIndexRightOutTupleList, outTupleListRight);
            propagateRight(rightTuple, newCompositeKey);
        }
    }

    private void propagateRight(UniTuple<Right_> rightTuple, Object compositeKey) {
        if (isFiltering) {
            // See the mirror comment in propagateLeft.
            crossMatchRight(rightTuple);
            return;
        }
        joinIndex.forEachLeft(rightTuple, compositeKey, leftTuple -> insertOutTupleIfActiveFiltered(leftTuple, rightTuple));
    }

    @Override
    public final void retractRight(UniTuple<Right_> rightTuple) {
        clearPendingRight(rightTuple);
        var compositeKey = rightTuple.removeStore(inputStoreIndexRightCompositeKey);
        if (compositeKey == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            return;
        }
        TupleList<OutTuple_> outTupleListRight = rightTuple.removeStore(inputStoreIndexRightOutTupleList);
        ListEntry<UniTuple<Right_>> entry = rightTuple.removeStore(inputStoreIndexRightEntry);
        joinIndex.removeRight(rightTuple, compositeKey, entry);
        outTupleListRight.clear(this::retractOutTupleRight);
    }

    @Override
    protected void reconcilePendingLeft(LeftTuple_ leftTuple) {
        var compositeKey = leftTuple.getStore(inputStoreIndexLeftCompositeKey);
        innerUpdateLeft(leftTuple, consumer -> joinIndex.forEachRight(leftTuple, compositeKey, consumer));
    }

    @Override
    protected void reconcilePendingRight(UniTuple<Right_> rightTuple) {
        var compositeKey = rightTuple.getStore(inputStoreIndexRightCompositeKey);
        innerUpdateRight(rightTuple, consumer -> joinIndex.forEachLeft(rightTuple, compositeKey, consumer));
    }

}
