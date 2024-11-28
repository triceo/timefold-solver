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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * By extensive benchmarking,
 * we have proven that (for a small number of keys) a list-based map is faster than {@link HashMap}.
 * This class implements a map that starts as a list.
 * It switches to a hash-based map when a certain number of keys is reached,
 * but it never switches back to a list if the number of keys goes back below the threshold.
 * The list is only instantiated when the first key is added,
 * and therefore the class has no memory overhead when no keys are added.
 * <p>
 * Besides the methods required by the {@link AbstractMap} contract,
 * we also implement additional methods commonly used throughout the solver codebase,
 * to get better performance there as well.
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
                addFirstEntry(key, value);
                return null;
            }
            var entry = getEntryFromList(key);
            if (entry != null) {
                return entry.setValue(value);
            }
            addEntry(key, value); // Key was not found, add a new entry.
            return null;
        }
    }

    private void addFirstEntry(K key, V value) {
        entryList = new ArrayList<>(THRESHOLD);
        entryList.add(new CustomEntry<>(key, value));
    }

    private void addEntry(K key, V value) {
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
    }

    @Override
    public Set<Entry<K, V>> entrySet() { // Required by AbstractMap contract.
        return useMap ? entryMap.entrySet()
                : entryList == null ? Collections.emptySet() : new ListBackedShrinkableSet<>(entryList);
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        // Although not required by AbstractMap contract, we can be more efficient.
        if (useMap) {
            return entryMap.computeIfAbsent(key, mappingFunction);
        }
        var value = mappingFunction.apply(key);
        if (entryList == null) { // First entry.
            addFirstEntry(key, value);
            return value;
        }
        var currentEntry = getEntryFromList(key);
        if (currentEntry != null) {
            return currentEntry.getValue();
        }
        addEntry(key, value); // Key was not found, add a new entry.
        return value;
    }

    private Entry<K, V> getEntryFromList(Object key) {
        for (var e : entryList) {
            if (Objects.equals(e.getKey(), key)) {
                return e;
            }
        }
        return null;
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        // Although not required by AbstractMap contract, we can be more efficient.
        if (useMap) {
            return entryMap.compute(key, remappingFunction);
        }
        var isListNull = entryList == null;
        var currentEntry = isListNull ? null : getEntryFromList(key);
        if (currentEntry == null) {
            var newValue = remappingFunction.apply(key, null);
            if (newValue == null) {
                return null;
            } else {
                if (isListNull) {
                    addFirstEntry(key, newValue);
                } else {
                    addEntry(key, newValue);
                }
                return newValue;
            }
        } else {
            var newValue = remappingFunction.apply(key, currentEntry.getValue());
            if (newValue == null) {
                entryList.remove(currentEntry);
                return null;
            } else {
                currentEntry.setValue(newValue);
                return newValue;
            }
        }
    }

    @Override
    public V get(Object key) { // Although not required by AbstractMap contract, we can be more efficient.
        if (useMap) {
            return entryMap.get(key);
        } else if (entryList == null) {
            return null;
        } else {
            var entry = getEntryFromList(key);
            if (entry == null) {
                return null;
            }
            return entry.getValue();
        }
    }

    @Override
    public V remove(Object key) { // Although not required by AbstractMap contract, we can be more efficient.
        if (useMap) {
            return entryMap.remove(key);
        } else if (entryList != null) {
            for (var i = 0; i < entryList.size(); i++) {
                var entry = entryList.get(i);
                if (Objects.equals(entry.getKey(), key)) {
                    entryList.remove(i);
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    @Override
    public void clear() {
        if (useMap) { // Go back to list-backed mode.
            entryMap.clear();
        } else if (entryList != null) {
            entryList.clear();
        }
    }

    /**
     * Exists to support null keys, which {@link Map#entry(Object, Object)} does not.
     */
    private static final class CustomEntry<K, V> implements Entry<K, V> {

        private final K key;
        private V value;

        private CustomEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

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
            var oldValue = this.value;
            this.value = value;
            return oldValue;
        }

    }

}
