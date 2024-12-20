package ai.timefold.solver.core.impl.score.stream.bavet.common;

import java.util.function.Function;

import ai.timefold.solver.core.impl.score.stream.bavet.common.index.IndexProperties;
import ai.timefold.solver.core.impl.score.stream.bavet.common.index.Indexer;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.AbstractTuple;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.LeftTupleLifecycle;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.RightTupleLifecycle;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.TupleLifecycle;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.UniTuple;
import ai.timefold.solver.core.impl.util.ElementAwareList;
import ai.timefold.solver.core.impl.util.ElementAwareListEntry;

/**
 * There is a strong likelihood that any change to this class, which is not related to indexing,
 * should also be made to {@link AbstractUnindexedJoinNode}.
 *
 * @param <LeftTuple_>
 * @param <Right_>
 */
public abstract class AbstractIndexedJoinNode<LeftTuple_ extends AbstractTuple, Right_, OutTuple_ extends AbstractTuple>
        extends AbstractJoinNode<LeftTuple_, Right_, OutTuple_>
        implements LeftTupleLifecycle<LeftTuple_>, RightTupleLifecycle<UniTuple<Right_>> {

    private final Function<Right_, IndexProperties> mappingRight;
    /**
     * Calls for example {@link AbstractScorer#insert(AbstractTuple)} and/or ...
     */
    private final Indexer<LeftTuple_> indexerLeft;
    private final Indexer<UniTuple<Right_>> indexerRight;

    protected AbstractIndexedJoinNode(Function<Right_, IndexProperties> mappingRight, int inputStoreIndexLeftOutTupleList,
            int inputStoreIndexRightOutTupleList, TupleLifecycle<OutTuple_> nextNodesTupleLifecycle, boolean isFiltering,
            int outputStoreIndexLeftOutEntry, int outputStoreIndexRightOutEntry, Indexer<LeftTuple_> indexerLeft,
            Indexer<UniTuple<Right_>> indexerRight) {
        super(inputStoreIndexLeftOutTupleList, inputStoreIndexRightOutTupleList, nextNodesTupleLifecycle, isFiltering,
                outputStoreIndexLeftOutEntry, outputStoreIndexRightOutEntry);
        this.mappingRight = mappingRight;
        this.indexerLeft = indexerLeft;
        this.indexerRight = indexerRight;
    }

    @Override
    public final void insertLeft(LeftTuple_ leftTuple) {
        IndexedJoinStore<LeftTuple_, OutTuple_> leftStore = leftTuple.getStore(inputStoreIndexLeft,
                IndexedJoinStore::new);
        if (leftStore.indexProperties != null) {
            throw new IllegalStateException("Impossible state: the input for the tuple (" + leftTuple
                    + ") was already added in the tupleStore.");
        }
        IndexProperties indexProperties = createIndexPropertiesLeft(leftTuple);
        leftStore.outList = new ElementAwareList<>();
        indexAndPropagateLeft(leftTuple, leftStore, indexProperties);
    }

    @Override
    public final void updateLeft(LeftTuple_ leftTuple) {
        IndexedJoinStore<LeftTuple_, OutTuple_> leftStore =
                leftTuple.getStore(inputStoreIndexLeft, IndexedJoinStore::new);
        IndexProperties oldIndexProperties = leftStore.indexProperties;
        if (oldIndexProperties == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            insertLeft(leftTuple);
            return;
        }
        IndexProperties newIndexProperties = createIndexPropertiesLeft(leftTuple);
        if (oldIndexProperties.equals(newIndexProperties)) {
            // No need for re-indexing because the index properties didn't change
            // Prefer an update over retract-insert if possible
            innerUpdateLeft(leftTuple, leftStore, consumer -> indexerRight.forEach(oldIndexProperties, consumer));
        } else {
            indexerLeft.remove(oldIndexProperties, leftStore.entry);
            leftStore.outList.forEach(this::retractOutTuple);
            // outList is now empty, no need to set leftStore.outList.
            indexAndPropagateLeft(leftTuple, leftStore, newIndexProperties);
        }
    }

    private void indexAndPropagateLeft(LeftTuple_ leftTuple, IndexedJoinStore<LeftTuple_, OutTuple_> leftStore,
            IndexProperties indexProperties) {
        leftStore.indexProperties = indexProperties;
        leftStore.entry = indexerLeft.put(indexProperties, leftTuple);
        indexerRight.forEach(indexProperties, rightTuple -> {
            IndexedJoinStore<UniTuple<Right_>, OutTuple_> rightStore = rightTuple.getStore(inputStoreIndexRight);
            insertOutTupleFiltered(leftTuple, leftStore, rightTuple, rightStore);
        });
    }

    @Override
    public final void retractLeft(LeftTuple_ leftTuple) {
        IndexedJoinStore<LeftTuple_, OutTuple_> leftStore = leftTuple.removeStore(inputStoreIndexLeft);
        if (leftStore == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            return;
        }
        ElementAwareListEntry<LeftTuple_> leftEntry = leftStore.entry;
        ElementAwareList<OutTuple_> outTupleListLeft = leftStore.outList;
        indexerLeft.remove(leftStore.indexProperties, leftEntry);
        outTupleListLeft.forEach(this::retractOutTuple);
    }

    @Override
    public final void insertRight(UniTuple<Right_> rightTuple) {
        IndexedJoinStore<UniTuple<Right_>, OutTuple_> rightStore = rightTuple.getStore(inputStoreIndexRight,
                IndexedJoinStore::new);
        if (rightStore.indexProperties != null) {
            throw new IllegalStateException("Impossible state: the input for the tuple (" + rightTuple
                    + ") was already added in the tupleStore.");
        }
        IndexProperties indexProperties = mappingRight.apply(rightTuple.factA);
        rightStore.outList = new ElementAwareList<>();
        indexAndPropagateRight(rightTuple, rightStore, indexProperties);
    }

    @Override
    public final void updateRight(UniTuple<Right_> rightTuple) {
        IndexedJoinStore<UniTuple<Right_>, OutTuple_> rightStore =
                rightTuple.getStore(inputStoreIndexRight, IndexedJoinStore::new);
        IndexProperties oldIndexProperties = rightStore.indexProperties;
        if (oldIndexProperties == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            insertRight(rightTuple);
            return;
        }
        IndexProperties newIndexProperties = mappingRight.apply(rightTuple.factA);
        if (oldIndexProperties.equals(newIndexProperties)) {
            // No need for re-indexing because the index properties didn't change
            // Prefer an update over retract-insert if possible
            innerUpdateRight(rightTuple, rightStore, consumer -> indexerLeft.forEach(oldIndexProperties, consumer));
        } else {
            indexerRight.remove(oldIndexProperties, rightStore.entry);
            rightStore.outList.forEach(this::retractOutTuple);
            // outList is now empty, no need for to set rightStore.outList.
            indexAndPropagateRight(rightTuple, rightStore, newIndexProperties);
        }
    }

    private void indexAndPropagateRight(UniTuple<Right_> rightTuple, IndexedJoinStore<UniTuple<Right_>, OutTuple_> rightStore,
            IndexProperties indexProperties) {
        rightStore.indexProperties = indexProperties;
        rightStore.entry = indexerRight.put(indexProperties, rightTuple);
        indexerLeft.forEach(indexProperties, leftTuple -> {
            IndexedJoinStore<LeftTuple_, OutTuple_> leftStore = leftTuple.getStore(inputStoreIndexLeft);
            insertOutTupleFiltered(leftTuple, leftStore, rightTuple, rightStore);
        });
    }

    @Override
    public final void retractRight(UniTuple<Right_> rightTuple) {
        IndexedJoinStore<UniTuple<Right_>, OutTuple_> rightStore = rightTuple.removeStore(inputStoreIndexRight);
        if (rightStore == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            return;
        }
        indexerRight.remove(rightStore.indexProperties, rightStore.entry);
        rightStore.outList.forEach(this::retractOutTuple);
    }

    protected abstract IndexProperties createIndexPropertiesLeft(LeftTuple_ leftTuple);

    static final class IndexedJoinStore<Tuple_ extends AbstractTuple, OutTuple_ extends AbstractTuple>
            extends AbstractJoinStore<OutTuple_> {

        private IndexProperties indexProperties;
        private ElementAwareListEntry<Tuple_> entry;

    }

}
