package ai.timefold.solver.core.impl.bavet.common.index;

import java.util.function.Consumer;

import ai.timefold.solver.core.impl.util.ElementAwareList;
import ai.timefold.solver.core.impl.util.ElementAwareListEntry;

// To avoid needless indirection and save memory, the indexer extends the list instead of wrapping it.
// Only operate this class through the Indexer interface.
final class NoneIndexer<T>
        extends ElementAwareList<T>
        implements Indexer<T> {

    @Override
    public ElementAwareListEntry<T> put(Object indexKeys, T tuple) {
        return add(tuple);
    }

    @Override
    public void remove(Object indexKeys, ElementAwareListEntry<T> entry) {
        entry.remove();
    }

    @Override
    public int size(Object indexKeys) {
        return size();
    }

    @Override
    public void forEach(Object indexKeys, Consumer<T> tupleConsumer) {
        forEach(tupleConsumer);
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public String toString() {
        return "size = " + size();
    }

}
