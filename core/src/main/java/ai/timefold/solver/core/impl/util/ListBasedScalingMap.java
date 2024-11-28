package ai.timefold.solver.core.impl.util;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntFunction;

public final class ListBasedScalingMap<K, V> extends AbstractMap<K, V> {

    public static <K, V> Map<K, V> create() {
        return new ListBasedScalingMap<>(CollectionUtils::newHashMap);
    }

    public static <K, V> Map<K, V> createLinked() {
        return new ListBasedScalingMap<>(CollectionUtils::newLinkedHashMap);
    }

    private static final int THRESHOLD = 5;

    private final IntFunction<Map<K, V>> mapSupplier;

    private boolean useMap = false;
    private List<Entry<K, V>> entryList;
    private Map<K, V> entryMap = null;

    private ListBasedScalingMap(IntFunction<Map<K, V>> mapSupplier) {
        this.mapSupplier = mapSupplier;
    }

    @Override
    public V put(K key, V value) {
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
    public Set<Entry<K, V>> entrySet() {
        return useMap ? entryMap.entrySet()
                : entryList == null ? Collections.emptySet() : new ListBackedShrinkingSet<>(entryList);
    }

    @Override
    public V get(Object key) {
        return useMap ? entryMap.get(key) : super.get(key);
    }

    @Override
    public V remove(Object key) {
        return useMap ? entryMap.remove(key) : super.remove(key);
    }

    @Override
    public void clear() {
        if (useMap) {
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
