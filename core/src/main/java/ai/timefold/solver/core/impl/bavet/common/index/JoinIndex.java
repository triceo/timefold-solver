package ai.timefold.solver.core.impl.bavet.common.index;

import java.util.function.Consumer;

import ai.timefold.solver.core.impl.bavet.common.tuple.Tuple;
import ai.timefold.solver.core.impl.util.ListEntry;

import org.jspecify.annotations.NullMarked;

/**
 * The index of a node with two inputs (join, ifExists), for both of its sides.
 * The left side stores elements of left tuples, the right side stores right tuples;
 * a query on one side uses the composite key of a tuple of the other side.
 * <p>
 * Every method takes the tuple that owns the stored element (or that queries).
 * An implementation may keep its own data in tuple-store slots it reserved when it was built,
 * so the owner must be the same tuple for every call about one element,
 * and a query must come from a tuple that is currently stored on its own side.
 * <p>
 * The node calls these methods on its hottest path.
 * Keep at most two implementations, so that those call sites stay bimorphic at worst.
 *
 * @param <L> the element type of the left side
 * @param <R> the element type of the right side
 */
@NullMarked
public interface JoinIndex<L, R> {

    ListEntry<L> putLeft(Tuple leftOwner, Object leftCompositeKey, L element);

    /**
     * Moves an element to a new composite key.
     *
     * @return the entry that replaces {@code entry}
     */
    ListEntry<L> updateLeft(Tuple leftOwner, Object oldLeftCompositeKey, Object newLeftCompositeKey, ListEntry<L> entry);

    void removeLeft(Tuple leftOwner, Object leftCompositeKey, ListEntry<L> entry);

    /**
     * Visits the left elements that match a right tuple.
     */
    void forEachLeft(Tuple rightOwner, Object rightCompositeKey, Consumer<L> consumer);

    ListEntry<R> putRight(Tuple rightOwner, Object rightCompositeKey, R element);

    /**
     * Moves an element to a new composite key.
     *
     * @return the entry that replaces {@code entry}
     */
    ListEntry<R> updateRight(Tuple rightOwner, Object oldRightCompositeKey, Object newRightCompositeKey, ListEntry<R> entry);

    void removeRight(Tuple rightOwner, Object rightCompositeKey, ListEntry<R> entry);

    /**
     * Visits the right elements that match a left tuple.
     */
    void forEachRight(Tuple leftOwner, Object leftCompositeKey, Consumer<R> consumer);

    /**
     * @return the number of right elements that match a left tuple
     */
    int sizeRight(Tuple leftOwner, Object leftCompositeKey);

}
