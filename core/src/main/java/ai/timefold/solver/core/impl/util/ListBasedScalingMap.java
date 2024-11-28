package ai.timefold.solver.core.impl.util;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntFunction;

/**
 * By extensive benchmarking,
 * we have proven that (for a small number of keys) a list-based map is faster than {@link HashMap}.
 * This class implements a map that starts as a list,
 * and then switches to a hash-based map when a certain threshold is reached.
 * It never switches back to a list, unless {@link #clear()} is called.
 * The list is only instantiated when the first key is added,
 * and therefore the map has no memory overhead when no keys are added.
 * <p>
 * Use {@link #create()} to create an instance that will eventually use a {@link HashMap}.
 * Use {@link #createLinked()} to create an instance that will eventually use a {@link LinkedHashMap}.
 * 
 * @param <K>
 * @param <V>
 */
public final class ListBasedScalingMap<K, V> extends AbstractMap<K, V> {

    public static <K, V> Map<K, V> create() {
        return new ListBasedScalingMap<>(CollectionUtils::newHashMap);
    }

    public static <K, V> Map<K, V> createLinked() {
        return new ListBasedScalingMap<>(CollectionUtils::newLinkedHashMap);
    }

    /**
     * The threshold at which the map switches from a list to a hash map.
     * This value was determined by extensive benchmarking.
     */
    private static final int THRESHOLD = 5;

    private final IntFunction<Map<K, V>> mapSupplier;

    private boolean useMap = false;
    private List<Entry<K, V>> entryList;
    private Map<K, V> entryMap = null;

    private ListBasedScalingMap(IntFunction<Map<K, V>> mapSupplier) {
        this.mapSupplier = mapSupplier;
    }

    @Override
    public V put(K key, V value) { // Required by AbstractMap contract.
        if (useMap) {
            return entryMap.put(key, value);
        } else {
            if (entryList == null) {
                entryList = new ArrayList<>(THRESHOLD);
            }
            for (int i = 0; i < entryList.size(); i++) {
                var e = entryList.get(i);
                if (Objects.equals(e.getKey(), key)) {
                    entryList.set(i, new CustomEntry<>(key, value));
                    return e.getValue();
                }
            }
            // Key was not found, add a new entry.
            if (entryList.size() == THRESHOLD) {
                entryMap = mapSupplier.apply(THRESHOLD * 2);
                for (var e : entryList) {
                    entryMap.put(e.getKey(), e.getValue());
                }
                entryMap.put(key, value);
                entryList = null;
                useMap = true;
            } else {
                entryList.add(new CustomEntry<>(key, value));
            }
            return null;
        }
    }

    @Override
    public Set<Entry<K, V>> entrySet() { // Required by AbstractMap contract.
        return useMap ? entryMap.entrySet()
                : entryList == null ? Collections.emptySet() : new ListBackedShrinkableSet<>(entryList);
    }

    @Override
    public V get(Object key) {
        // Although not required by AbstractMap contract,
        // we can be more efficient if we know we're backed by a hash map.
        return useMap ? entryMap.get(key) : super.get(key);
    }

    @Override
    public V remove(Object key) {
        // Although not required by AbstractMap contract,
        // we can be more efficient if we know we're backed by a hash map.
        return useMap ? entryMap.remove(key) : super.remove(key);
    }

    @Override
    public void clear() {
        if (useMap) { // Go back to list-backed mode.
            entryMap = null;
            useMap = false;
        } else if (entryList != null) {
            entryList.clear();
        }
    }

    /**
     * Exists to support null keys, which {@link Map#entry(Object, Object)} does not.
     */
    private record CustomEntry<K, V>(K key, V value) implements Entry<K, V> {
        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }
    }

}
