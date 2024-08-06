package ai.timefold.solver.core.impl.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

public final class SortingIterator<T> implements Iterator<T> {

    private final Iterator<T> listIterator;

    public SortingIterator(Iterator<T> iterator, Comparator<T> comparator) {
        var list = new ArrayList<T>();
        iterator.forEachRemaining(list::add);
        list.sort(comparator);
        this.listIterator = list.iterator();
    }

    @Override
    public boolean hasNext() {
        return listIterator.hasNext();
    }

    @Override
    public T next() {
        return listIterator.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
