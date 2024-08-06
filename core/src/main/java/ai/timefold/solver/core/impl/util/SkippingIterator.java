package ai.timefold.solver.core.impl.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public final class SkippingIterator<T> implements Iterator<T> {

    private final Iterator<T> iterator;
    private final Predicate<T> predicate;
    private T nextElement;
    private boolean hasNextElement;

    public SkippingIterator(Iterator<T> iterator, Predicate<T> predicate) {
        this.iterator = iterator;
        this.predicate = predicate;
        advance();
    }

    private void advance() {
        hasNextElement = false;
        while (iterator.hasNext()) {
            T element = iterator.next();
            if (!predicate.test(element)) {
                nextElement = element;
                hasNextElement = true;
                break;
            }
        }
    }

    @Override
    public boolean hasNext() {
        return hasNextElement;
    }

    @Override
    public T next() {
        if (!hasNextElement) {
            throw new NoSuchElementException();
        }
        T result = nextElement;
        advance();
        return result;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
