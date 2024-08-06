package ai.timefold.solver.core.impl.util;

import java.util.Iterator;
import java.util.function.Function;

public class MappingIterator<T, R> implements Iterator<R> {

    private final Iterator<T> iterator;
    private final Function<? super T, ? extends R> mapper;

    public MappingIterator(Iterator<T> iterator, Function<? super T, ? extends R> mapper) {
        this.iterator = iterator;
        this.mapper = mapper;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public R next() {
        T nextValue = iterator.next();
        return mapper.apply(nextValue);
    }
}