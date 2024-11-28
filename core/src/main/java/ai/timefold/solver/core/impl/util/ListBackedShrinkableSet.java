package ai.timefold.solver.core.impl.util;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * A set backed by a list.
 * Exists to support {@link ListBasedScalingMap#entrySet()}.
 * It only works as a set as long as the list contains unique elements.
 * <p>
 * The set does not support {@link #add(Object)} or {@link #remove(Object)},
 * but the backing list can be modified directly and the modifications will be immediately reflected in the set.
 * The iterator supports {@link Iterator#remove()} which propagates to the backing list,
 * as that is required for {@link ListBasedScalingMap#entrySet()}.
 * 
 * @param <E>
 */
final class ListBackedShrinkableSet<E> extends AbstractSet<E> {

    private final List<E> list;

    public ListBackedShrinkableSet(List<E> list) {
        this.list = list;
    }

    @Override
    public Iterator<E> iterator() { // Required by AbstractSet contract.
        return list.iterator();
    }

    @Override
    public int size() { // Required by AbstractSet contract.
        return list.size();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ListBackedShrinkableSet<?> other)) {
            return false;
        }
        return Objects.equals(list, other.list);
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }
}
