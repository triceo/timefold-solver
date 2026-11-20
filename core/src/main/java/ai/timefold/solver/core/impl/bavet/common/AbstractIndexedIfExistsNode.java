package ai.timefold.solver.core.impl.bavet.common;

import ai.timefold.solver.core.impl.bavet.common.index.AbstractIndexerFactory;
import ai.timefold.solver.core.impl.bavet.common.index.AbstractIndexerFactory.KeysExtractor;
import ai.timefold.solver.core.impl.bavet.common.index.AbstractIndexerFactory.UniKeysExtractor;
import ai.timefold.solver.core.impl.bavet.common.index.JoinIndex;
import ai.timefold.solver.core.impl.bavet.common.tuple.InTupleStorePositionTracker;
import ai.timefold.solver.core.impl.bavet.common.tuple.LeftTupleLifecycle;
import ai.timefold.solver.core.impl.bavet.common.tuple.RightTupleLifecycle;
import ai.timefold.solver.core.impl.bavet.common.tuple.Tuple;
import ai.timefold.solver.core.impl.bavet.common.tuple.TupleLifecycle;
import ai.timefold.solver.core.impl.bavet.common.tuple.UniTuple;
import ai.timefold.solver.core.impl.bavet.common.tuple.indictment.IndictmentSource;
import ai.timefold.solver.core.impl.util.ListEntry;

/**
 * There is a strong likelihood that any change to this class,
 * which is not related to indexing,
 * should also be made to {@link AbstractUnindexedIfExistsNode}.
 * <p>
 * Both sides are indexed in one {@link JoinIndex}:
 * the left side holds the counters of the left tuples, the right side holds the right tuples.
 * The opposite side is always read before this side of a tuple is updated or removed.
 * The counter / filtering-tracker logic is in {@link AbstractIfExistsNode}.
 *
 * @param <LeftTuple_>
 * @param <Right_>
 */
public abstract class AbstractIndexedIfExistsNode<LeftTuple_ extends Tuple, Right_>
        extends AbstractIfExistsNode<LeftTuple_, Right_>
        implements LeftTupleLifecycle<LeftTuple_>, RightTupleLifecycle<UniTuple<Right_>> {

    private final KeysExtractor<LeftTuple_> keysExtractorLeft;
    private final UniKeysExtractor<Right_> keysExtractorRight;
    private final int inputStoreIndexLefCompositeKey;
    private final int inputStoreIndexLeftCounterEntry;
    private final int inputStoreIndexRightCompositeKey;
    private final int inputStoreIndexRightEntry;
    private final JoinIndex<ExistsCounter<LeftTuple_>, UniTuple<Right_>> joinIndex;

    protected AbstractIndexedIfExistsNode(boolean shouldExist, KeysExtractor<LeftTuple_> keysExtractorLeft,
            AbstractIndexerFactory<Right_> indexerFactory, TupleLifecycle<LeftTuple_> nextNodesTupleLifecycle,
            boolean isFiltering,
            InTupleStorePositionTracker tupleStorePositionTracker) {
        super(shouldExist, nextNodesTupleLifecycle, isFiltering, tupleStorePositionTracker);
        this.keysExtractorLeft = keysExtractorLeft;
        this.keysExtractorRight = indexerFactory.buildRightKeysExtractor();
        this.inputStoreIndexLefCompositeKey = tupleStorePositionTracker.reserveNextLeft();
        this.inputStoreIndexLeftCounterEntry = tupleStorePositionTracker.reserveNextLeft();
        this.inputStoreIndexRightCompositeKey = tupleStorePositionTracker.reserveNextRight();
        this.inputStoreIndexRightEntry = tupleStorePositionTracker.reserveNextRight();
        this.joinIndex = indexerFactory.buildJoinIndex(tupleStorePositionTracker);
    }

    @Override
    public final void insertLeft(LeftTuple_ leftTuple) {
        if (leftTuple.getStore(inputStoreIndexLefCompositeKey) != null) {
            throw new IllegalStateException(
                    "Impossible state: the input for the tuple (%s) was already added in the tupleStore.".formatted(leftTuple));
        }
        var compositeKey = keysExtractorLeft.apply(leftTuple);
        leftTuple.setStore(inputStoreIndexLefCompositeKey, compositeKey);

        var counter = new ExistsCounter<>(leftTuple);
        var counterEntry = joinIndex.putLeft(leftTuple, compositeKey, counter);
        leftTuple.setStore(inputStoreIndexLeftCounterEntry, counterEntry);
        if (!isFiltering) {
            counter.countRight = joinIndex.sizeRight(leftTuple, compositeKey);
            initCounterLeft(counter);
            if (leftTuple.getIndictmentSource() != IndictmentSource.DISABLED) {
                joinIndex.forEachRight(leftTuple, compositeKey, rightTuple -> {
                    IndictmentSource.addCorroborator(getId(), leftTuple, rightTuple);
                });
            }
        } else {
            // Defer the cross-match (the opposite-side read) to this node's own layer turn instead of computing it now,
            // at whatever layer the parent that produced leftTuple happens to be in.
            // See AbstractCrossMatchNode's pendingLeft/pendingRight javadoc.
            crossMatchLeft(leftTuple);
        }
    }

    @Override
    public final void updateLeft(LeftTuple_ leftTuple) {
        var oldCompositeKey = leftTuple.getStore(inputStoreIndexLefCompositeKey);
        if (oldCompositeKey == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            insertLeft(leftTuple);
            return;
        }
        var newCompositeKey = keysExtractorLeft.apply(leftTuple);
        ListEntry<ExistsCounter<LeftTuple_>> counterEntry = leftTuple.getStore(inputStoreIndexLeftCounterEntry);
        var counter = counterEntry.element();

        if (oldCompositeKey.equals(newCompositeKey)) {
            // No need for re-indexing because the index keys didn't change
            // The indexers contain counters in the DEAD state, to track the rightCount.
            if (!isFiltering) {
                updateUnchangedCounterLeft(counter);
                if (leftTuple.getIndictmentSource() != IndictmentSource.DISABLED) {
                    IndictmentSource.clearCorroborators(getId(), leftTuple);
                    joinIndex.forEachRight(leftTuple, newCompositeKey, rightTuple -> {
                        IndictmentSource.addCorroborator(getId(), leftTuple, rightTuple);
                    });
                }
            } else {
                // Eager own-side cleanup (reads only pairs already tracked, so it's eager-safe),
                // then defer the re-walk of the opposite side to this node's own layer turn.
                // See AbstractCrossMatchNode's pendingLeft/pendingRight javadoc.
                clearLeftTrackerList(leftTuple);
                counter.countRight = 0;
                IndictmentSource.clearCorroborators(getId(), leftTuple);
                crossMatchLeft(leftTuple);
            }
        } else {
            clearLeftTrackerList(leftTuple);
            counter.countRight = 0;
            IndictmentSource.clearCorroborators(getId(), leftTuple);
            leftTuple.setStore(inputStoreIndexLefCompositeKey, newCompositeKey);
            leftTuple.setStore(inputStoreIndexLeftCounterEntry,
                    joinIndex.updateLeft(leftTuple, oldCompositeKey, newCompositeKey, counterEntry));
            if (isFiltering) {
                crossMatchLeft(leftTuple);
            } else {
                counter.countRight = joinIndex.sizeRight(leftTuple, newCompositeKey);
                updateCounterLeft(counter);
                if (leftTuple.getIndictmentSource() != IndictmentSource.DISABLED) {
                    joinIndex.forEachRight(leftTuple, newCompositeKey, rightTuple -> {
                        IndictmentSource.addCorroborator(getId(), leftTuple, rightTuple);
                    });
                }
            }
        }
    }

    @Override
    public final void retractLeft(LeftTuple_ leftTuple) {
        clearPendingLeft(leftTuple); // A tuple can be retracted before its turn to reconcile ever comes.
        var compositeKey = leftTuple.removeStore(inputStoreIndexLefCompositeKey);
        if (compositeKey == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            return;
        }
        ListEntry<ExistsCounter<LeftTuple_>> counterEntry = leftTuple.removeStore(inputStoreIndexLeftCounterEntry);
        var element = counterEntry.element(); // Store so that the reference survives removal.
        joinIndex.removeLeft(leftTuple, compositeKey, counterEntry);
        clearLeftTrackerList(leftTuple);
        killCounterLeft(element);
    }

    @Override
    public final void insertRight(UniTuple<Right_> rightTuple) {
        if (rightTuple.getStore(inputStoreIndexRightCompositeKey) != null) {
            throw new IllegalStateException(
                    "Impossible state: the input for the tuple (%s) was already added in the tupleStore."
                            .formatted(rightTuple));
        }
        var compositeKey = keysExtractorRight.apply(rightTuple);
        rightTuple.setStore(inputStoreIndexRightCompositeKey, compositeKey);
        rightTuple.setStore(inputStoreIndexRightEntry, joinIndex.putRight(rightTuple, compositeKey, rightTuple));
        updateCounterLeft(rightTuple, compositeKey);
    }

    private void updateCounterLeft(UniTuple<Right_> rightTuple, Object compositeKey) {
        if (!isFiltering) {
            // To prevent creating a dynamic lambda on the hot path,
            // only call the 2-args version when indictments are enabled
            if (rightTuple.getIndictmentSource() == IndictmentSource.DISABLED) {
                joinIndex.forEachLeft(rightTuple, compositeKey, this::incrementCounterRight);
            } else {
                joinIndex.forEachLeft(rightTuple, compositeKey,
                        counter -> incrementCounterRightUpdatingIndictment(counter, rightTuple));
            }
        } else {
            // Defer the cross-match (the opposite-side read) to this node's own layer turn instead of computing it now,
            // at whatever layer the parent that produced rightTuple happens to be in.
            // See AbstractCrossMatchNode's pendingLeft/pendingRight javadoc.
            crossMatchRight(rightTuple);
        }
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
                // Eager own-side cleanup, then defer the re-walk of the opposite side.
                // See AbstractCrossMatchNode's pendingLeft/pendingRight javadoc.
                clearRightTrackerList(rightTuple);
                crossMatchRight(rightTuple);
            }
        } else {
            if (!isFiltering) {
                joinIndex.forEachLeft(rightTuple, oldCompositeKey, this::decrementCounterRight);
            } else {
                clearRightTrackerList(rightTuple);
            }
            ListEntry<UniTuple<Right_>> entry = rightTuple.getStore(inputStoreIndexRightEntry);
            rightTuple.setStore(inputStoreIndexRightCompositeKey, newCompositeKey);
            rightTuple.setStore(inputStoreIndexRightEntry,
                    joinIndex.updateRight(rightTuple, oldCompositeKey, newCompositeKey, entry));
            updateCounterLeft(rightTuple, newCompositeKey);
        }
    }

    @Override
    public final void retractRight(UniTuple<Right_> rightTuple) {
        clearPendingRight(rightTuple); // A tuple can be retracted before its turn to reconcile ever comes.
        var compositeKey = rightTuple.removeStore(inputStoreIndexRightCompositeKey);
        if (compositeKey == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            return;
        }
        if (!isFiltering) {
            // To prevent creating a dynamic lambda on the hot path,
            // only call the 2-args version when indictments are enabled
            if (rightTuple.getIndictmentSource() == IndictmentSource.DISABLED) {
                joinIndex.forEachLeft(rightTuple, compositeKey, this::decrementCounterRight);
            } else {
                joinIndex.forEachLeft(rightTuple, compositeKey,
                        counter -> decrementCounterRightUpdatingIndictment(counter, rightTuple));
            }
        } else {
            clearRightTrackerList(rightTuple);
        }
        ListEntry<UniTuple<Right_>> entry = rightTuple.removeStore(inputStoreIndexRightEntry);
        joinIndex.removeRight(rightTuple, compositeKey, entry);
    }

    @Override
    protected void reconcilePendingLeft(LeftTuple_ leftTuple) {
        var compositeKey = leftTuple.getStore(inputStoreIndexLefCompositeKey);
        ListEntry<ExistsCounter<LeftTuple_>> counterEntry = leftTuple.getStore(inputStoreIndexLeftCounterEntry);
        var counter = counterEntry.element();
        clearLeftTrackerList(leftTuple);
        IndictmentSource.clearCorroborators(getId(), leftTuple);
        counter.countRight = 0;
        joinIndex.forEachRight(leftTuple, compositeKey, rightTuple -> updateCounterLeft(counter, rightTuple));
        updateCounterLeft(counter);
    }

    @Override
    protected void reconcilePendingRight(UniTuple<Right_> rightTuple) {
        var compositeKey = rightTuple.getStore(inputStoreIndexRightCompositeKey);
        clearRightTrackerList(rightTuple);
        joinIndex.forEachLeft(rightTuple, compositeKey, counter -> updateCounterRight(counter, rightTuple));
    }

}
