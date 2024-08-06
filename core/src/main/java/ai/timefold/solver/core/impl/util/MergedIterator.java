package ai.timefold.solver.core.impl.util;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public final class MergedIterator<T> implements Iterator<T> {

    private final List<Iterator<T>> iterators;

    public MergedIterator(List<Iterator<T>> iterators) {
        this.iterators = iterators;
    }

    @Override
    public boolean hasNext() {
        if (iterators.isEmpty()) {
            return false;
        }
        while (!iterators.get(0).hasNext()) {
            iterators.remove(0);
            if (iterators.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return iterators.get(0).next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
