package ai.timefold.solver.core.impl.bavet.common.index;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import ai.timefold.solver.core.impl.util.ElementAwareListEntry;

final class EqualsIndexer<T, Key_> implements Indexer<T> {

    private final KeyRetriever<Key_> keyRetriever;
    private final Supplier<Indexer<T>> downstreamIndexerSupplier;
    private final Map<Key_, Indexer<T>> downstreamIndexerMap = new HashMap<>();

    /**
     * Construct an {@link EqualsIndexer} which immediately ends in a {@link NoneIndexer}.
     * This means {@code indexKeys} must be a single key.
     */
    public EqualsIndexer() {
        this.keyRetriever = new SingleKeyRetriever<>();
        this.downstreamIndexerSupplier = NoneIndexer::new;
    }

    /**
     * Construct an {@link EqualsIndexer} which does not immediately go to a {@link NoneIndexer}.
     * This means {@code indexKeys} must be an instance of {@link IndexKeys}.
     * 
     * @param keyIndex the index of the key to use within {@link IndexKeys}.
     * @param downstreamIndexerSupplier the supplier of the downstream indexer
     */
    public EqualsIndexer(int keyIndex, Supplier<Indexer<T>> downstreamIndexerSupplier) {
        this.keyRetriever = new ManyKeyRetriever<>(keyIndex);
        this.downstreamIndexerSupplier = Objects.requireNonNull(downstreamIndexerSupplier);
    }

    @Override
    public ElementAwareListEntry<T> put(Object indexKeys, T tuple) {
        Key_ indexKey = keyRetriever.apply(indexKeys);
        Indexer<T> downstreamIndexer = getDownstreamIndexer(indexKey);
        return downstreamIndexer.put(indexKeys, tuple);
    }

    private Indexer<T> getDownstreamIndexer(Key_ indexerKey) {
        // Does the map retrieval with minimal possible get() calls and no computeIfAbsent() calls.
        // Micro-optimization for the hot path.
        var downstreamIndexer = isEmpty() ? null : downstreamIndexerMap.get(indexerKey);
        if (downstreamIndexer == null) {
            downstreamIndexer = downstreamIndexerSupplier.get();
            downstreamIndexerMap.put(indexerKey, downstreamIndexer);
        }
        return downstreamIndexer;
    }

    @Override
    public void remove(Object indexKeys, ElementAwareListEntry<T> entry) {
        Key_ indexKey = keyRetriever.apply(indexKeys);
        Indexer<T> downstreamIndexer = getDownstreamIndexer(indexKeys, indexKey, entry);
        downstreamIndexer.remove(indexKeys, entry);
        if (downstreamIndexer.isEmpty()) {
            downstreamIndexerMap.remove(indexKey);
        }
    }

    private Indexer<T> getDownstreamIndexer(Object indexKeys, Key_ indexerKey, ElementAwareListEntry<T> entry) {
        Indexer<T> downstreamIndexer = downstreamIndexerMap.get(indexerKey);
        if (downstreamIndexer == null) {
            throw new IllegalStateException(
                    "Impossible state: the tuple (%s) with indexKey (%s) doesn't exist in the indexer %s."
                            .formatted(entry.getElement(), indexKeys, this));
        }
        return downstreamIndexer;
    }

    @Override
    public int size(Object indexKeys) {
        if (isEmpty()) { // Saves a lot of work on the hot path.
            return 0;
        }
        Key_ indexKey = keyRetriever.apply(indexKeys);
        Indexer<T> downstreamIndexer = downstreamIndexerMap.get(indexKey);
        if (downstreamIndexer == null) {
            return 0;
        }
        return downstreamIndexer.size(indexKeys);
    }

    @Override
    public void forEach(Object indexKeys, Consumer<T> tupleConsumer) {
        if (isEmpty()) { // Saves a lot of work on the hot path.
            return;
        }
        Key_ indexKey = keyRetriever.apply(indexKeys);
        Indexer<T> downstreamIndexer = downstreamIndexerMap.get(indexKey);
        if (downstreamIndexer == null) {
            return;
        }
        downstreamIndexer.forEach(indexKeys, tupleConsumer);
    }

    @Override
    public boolean isEmpty() {
        return downstreamIndexerMap.isEmpty();
    }

    @Override
    public String toString() {
        return "size = " + downstreamIndexerMap.size();
    }

}
