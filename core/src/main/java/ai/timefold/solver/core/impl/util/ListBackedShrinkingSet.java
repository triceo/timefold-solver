package ai.timefold.solver.core.impl.util;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;

final class ListBackedShrinkingSet<E> extends AbstractSet<E> {

    private final List<E> list;

    public ListBackedShrinkingSet(List<E> list) {
        this.list = list;
    }

    @Override
    public Iterator<E> iterator() {
        return list.iterator();
    }

    @Override
    public int size() {
        return list.size();
    }

}
