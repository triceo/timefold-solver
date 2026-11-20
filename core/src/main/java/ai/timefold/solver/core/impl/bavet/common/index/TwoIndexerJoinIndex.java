package ai.timefold.solver.core.impl.bavet.common.index;

import java.util.function.Consumer;

import ai.timefold.solver.core.impl.bavet.common.tuple.Tuple;
import ai.timefold.solver.core.impl.util.ListEntry;

import org.jspecify.annotations.NullMarked;

/**
 * A {@link JoinIndex} with one {@link Indexer} for each side.
 * A tuple queries the indexer of the other side with its own composite key.
 * Uses no tuple-store slots.
 *
 * @param <L> the element type of the left side
 * @param <R> the element type of the right side
 */
@NullMarked
public final class TwoIndexerJoinIndex<L, R> implements JoinIndex<L, R> {

    private final Indexer<L> indexerLeft;
    private final Indexer<R> indexerRight;

    public TwoIndexerJoinIndex(Indexer<L> indexerLeft, Indexer<R> indexerRight) {
        this.indexerLeft = indexerLeft;
        this.indexerRight = indexerRight;
    }

    @Override
    public ListEntry<L> putLeft(Tuple leftOwner, Object leftCompositeKey, L element) {
        return indexerLeft.put(leftCompositeKey, element);
    }

    @Override
    public ListEntry<L> updateLeft(Tuple leftOwner, Object oldLeftCompositeKey, Object newLeftCompositeKey,
            ListEntry<L> entry) {
        indexerLeft.remove(oldLeftCompositeKey, entry);
        return indexerLeft.put(newLeftCompositeKey, entry.element());
    }

    @Override
    public void removeLeft(Tuple leftOwner, Object leftCompositeKey, ListEntry<L> entry) {
        indexerLeft.remove(leftCompositeKey, entry);
    }

    @Override
    public void forEachLeft(Tuple rightOwner, Object rightCompositeKey, Consumer<L> consumer) {
        indexerLeft.forEach(rightCompositeKey, consumer);
    }

    @Override
    public ListEntry<R> putRight(Tuple rightOwner, Object rightCompositeKey, R element) {
        return indexerRight.put(rightCompositeKey, element);
    }

    @Override
    public ListEntry<R> updateRight(Tuple rightOwner, Object oldRightCompositeKey, Object newRightCompositeKey,
            ListEntry<R> entry) {
        indexerRight.remove(oldRightCompositeKey, entry);
        return indexerRight.put(newRightCompositeKey, entry.element());
    }

    @Override
    public void removeRight(Tuple rightOwner, Object rightCompositeKey, ListEntry<R> entry) {
        indexerRight.remove(rightCompositeKey, entry);
    }

    @Override
    public void forEachRight(Tuple leftOwner, Object leftCompositeKey, Consumer<R> consumer) {
        indexerRight.forEach(leftCompositeKey, consumer);
    }

    @Override
    public int sizeRight(Tuple leftOwner, Object leftCompositeKey) {
        return indexerRight.size(leftCompositeKey);
    }

    @Override
    public String toString() {
        return "left (%s), right (%s)".formatted(indexerLeft, indexerRight);
    }

}
