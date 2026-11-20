package ai.timefold.solver.core.impl.bavet.common.index;

import static ai.timefold.solver.core.impl.bavet.common.joiner.JoinerType.GREATER_THAN;
import static ai.timefold.solver.core.impl.bavet.common.joiner.JoinerType.GREATER_THAN_OR_EQUAL;
import static ai.timefold.solver.core.impl.bavet.common.joiner.JoinerType.LESS_THAN;
import static ai.timefold.solver.core.impl.bavet.common.joiner.JoinerType.LESS_THAN_OR_EQUAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import ai.timefold.solver.core.api.score.stream.Joiners;
import ai.timefold.solver.core.impl.bavet.bi.joiner.DefaultBiJoiner;
import ai.timefold.solver.core.impl.bavet.common.joiner.JoinerType;
import ai.timefold.solver.core.impl.neighborhood.stream.joiner.DefaultBiNeighborhoodsJoiner;
import ai.timefold.solver.core.impl.util.ListEntry;
import ai.timefold.solver.core.preview.api.neighborhood.stream.joiner.NeighborhoodsJoiners;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SortedBlockIndexerTest {

    private static final JoinerType[] COMPARISONS =
            { LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL };

    /**
     * One stored element and its keys, one key per level.
     */
    private record Stored(int id, Object compositeKey, int[] keys, ListEntry<Integer> entry) {
    }

    static Stream<Arguments> joinerTypeCombinations() {
        var combinations = new ArrayList<Arguments>();
        for (var first : COMPARISONS) {
            combinations.add(Arguments.of(List.of(first)));
            for (var second : COMPARISONS) {
                combinations.add(Arguments.of(List.of(first, second)));
                for (var third : COMPARISONS) {
                    combinations.add(Arguments.of(List.of(first, second, third)));
                }
            }
        }
        return combinations.stream();
    }

    @ParameterizedTest
    @MethodSource("joinerTypeCombinations")
    void randomOperationsMatchBruteForce(List<JoinerType> joinerTypes) {
        for (var isLeftBridge : new boolean[] { true, false }) {
            var random = new Random(37 + joinerTypes.hashCode() + (isLeftBridge ? 1 : 0));
            var harness = new Harness(isLeftBridge, joinerTypes);
            for (var step = 0; step < 3_000; step++) {
                // Grow to a few hundred entries (several blocks), then keep the size around there.
                if (harness.stored.isEmpty() || random.nextInt(10) < (harness.stored.size() < 400 ? 7 : 5)) {
                    harness.put(random, 25);
                } else {
                    harness.remove(random.nextInt(harness.stored.size()));
                }
                if (step % 7 == 0) {
                    harness.assertQuery(harness.randomKeys(random, 25));
                }
            }
            while (!harness.stored.isEmpty()) {
                harness.remove(random.nextInt(harness.stored.size()));
                if (harness.stored.size() % 11 == 0) {
                    harness.assertQuery(harness.randomKeys(random, 25));
                }
            }
            assertThat(harness.indexer.isRemovable()).isTrue();
        }
    }

    static Stream<Arguments> sizes() {
        return Stream.of(0, 1, 63, 64, 65, 5_000).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("sizes")
    void fillAndDrainInInsertionAndReverseOrder(int size) {
        for (var joinerType : COMPARISONS) {
            for (var reverse : new boolean[] { false, true }) {
                var random = new Random(size);
                var harness = new Harness(true, List.of(joinerType));
                for (var i = 0; i < size; i++) {
                    harness.put(random, 100);
                }
                harness.assertQueries(random);
                var insertionOrder = new ArrayList<>(harness.stored);
                if (reverse) {
                    Collections.reverse(insertionOrder);
                }
                var removedCount = 0;
                for (var stored : insertionOrder) {
                    harness.remove(harness.stored.indexOf(stored));
                    removedCount++;
                    if (removedCount % 50 == 0) {
                        harness.assertQuery(harness.randomKeys(random, 100));
                    }
                }
                harness.assertQueries(random);
                assertThat(harness.indexer.isRemovable()).isTrue();
            }
        }
    }

    @Test
    void manyEqualPrimaryKeysWithSecondary() {
        for (var isLeftBridge : new boolean[] { true, false }) {
            var random = new Random(7);
            var harness = new Harness(isLeftBridge, List.of(LESS_THAN, GREATER_THAN, LESS_THAN));
            for (var i = 0; i < 1_000; i++) {
                harness.put(random, 3); // Long runs of equal keys spread over several blocks.
            }
            harness.assertQueries(random);
            while (!harness.stored.isEmpty()) {
                harness.remove(random.nextInt(harness.stored.size()));
                if (harness.stored.size() % 37 == 0) {
                    harness.assertQueries(random);
                }
            }
        }
    }

    @Test
    void removeBestSecondaryKey() {
        var harness = new Harness(true, List.of(LESS_THAN, GREATER_THAN));
        // Primary key 0 for all; secondary keys 0..99; the best (maximum) is removed first.
        for (var i = 0; i < 100; i++) {
            harness.put(new int[] { 0, i });
        }
        for (var i = 99; i >= 0; i--) {
            var index = i;
            harness.remove(harness.indexOfKeys(new int[] { 0, index }));
            harness.assertQuery(new int[] { 1, i - 2 });
            harness.assertQuery(new int[] { 1, 0 });
        }
    }

    private record CountingKey(int value, AtomicInteger comparisonCount) implements Comparable<CountingKey> {

        @Override
        public int compareTo(CountingKey other) {
            comparisonCount.incrementAndGet();
            return Integer.compare(value, other.value);
        }

    }

    @Test
    void secondaryLevelSkipsBlocks() {
        // Overlapping intervals [start, start + 2): left stores (start, end); query (rightEnd, rightStart).
        var comparisonCount = new AtomicInteger();
        var indexer = new SortedBlockIndexer<Integer>(true, List.of(LESS_THAN, GREATER_THAN),
                List.of(KeyUnpacker.composite(0), KeyUnpacker.composite(1)));
        var entries = new ArrayList<ListEntry<Integer>>();
        for (var start = 0; start < 5_000; start++) {
            var compositeKey = CompositeKey.ofMany(new CountingKey(start, comparisonCount),
                    new CountingKey(start + 2, comparisonCount));
            entries.add(indexer.put(compositeKey, start));
        }
        var query = CompositeKey.ofMany(new CountingKey(4_001, comparisonCount), new CountingKey(4_000, comparisonCount));
        comparisonCount.set(0);
        var matches = new ArrayList<Integer>();
        indexer.forEach(query, matches::add);
        assertThat(matches).containsExactlyInAnyOrder(3_999, 4_000);
        // Without the block skip, about 4 000 entries pass the primary test and each needs a comparison.
        assertThat(comparisonCount.get()).isLessThan(800);
    }

    @Test
    void selectPairStart() {
        assertThat(SortedBlockIndexer.selectPairStart(List.of(GREATER_THAN, LESS_THAN, GREATER_THAN))).isEqualTo(1);
        assertThat(SortedBlockIndexer.selectPairStart(List.of(LESS_THAN, GREATER_THAN, LESS_THAN))).isZero();
        assertThat(SortedBlockIndexer.selectPairStart(List.of(GREATER_THAN, LESS_THAN_OR_EQUAL))).isZero();
        assertThat(SortedBlockIndexer.selectPairStart(List.of(LESS_THAN))).isEqualTo(-1);
        assertThat(SortedBlockIndexer.selectPairStart(List.of(GREATER_THAN, GREATER_THAN))).isEqualTo(-1);
    }

    @Test
    void nullKeyFails() {
        KeyUnpacker<Integer> nullKeyUnpacker = compositeKey -> null;
        var indexer = new SortedBlockIndexer<String>(true, List.of(LESS_THAN), List.of(nullKeyUnpacker));
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> indexer.put(1, "a"))
                .withMessageContaining("is null")
                .withMessageContaining("Maybe");
    }

    @Test
    void removeUnknownEntryFails() {
        var indexer = new SortedBlockIndexer<String>(true, List.of(LESS_THAN), List.of(KeyUnpacker.single()));
        var entry = indexer.put(1, "a");
        indexer.remove(1, entry);
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> indexer.remove(1, entry))
                .withMessageContaining("Impossible state");
    }

    @Test
    void randomAccessIsNotSupported() {
        var indexer = new SortedBlockIndexer<String>(true, List.of(LESS_THAN), List.of(KeyUnpacker.single()));
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> indexer.randomIterator(1, new Random(0)));
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> indexer.uniqueRandomIterator(1, new Random(0)));
    }

    @Test
    void factoryRouting() {
        var lessThan = (DefaultBiJoiner<TestPerson, TestPerson>) Joiners.lessThan(TestPerson::age);
        var lessThanGreaterThan = (DefaultBiJoiner<TestPerson, TestPerson>) Joiners.lessThan(TestPerson::age)
                .and(Joiners.greaterThan(TestPerson::age));
        var equalLessThan = (DefaultBiJoiner<TestPerson, TestPerson>) Joiners.equal(TestPerson::gender)
                .and(Joiners.lessThan(TestPerson::age));
        var equalOnly = (DefaultBiJoiner<TestPerson, TestPerson>) Joiners.equal(TestPerson::gender);
        var lessThanContaining = (DefaultBiJoiner<TestPerson, List<Integer>>) Joiners
                .<TestPerson, List<Integer>, Integer> lessThan(TestPerson::age, list -> list.get(0))
                .and(Joiners.containing(person -> List.of(person.age()), list -> list.get(0)));
        var neighborhoodsLessThan =
                (DefaultBiNeighborhoodsJoiner<TestPerson, TestPerson>) NeighborhoodsJoiners.lessThan(TestPerson::age);

        assertThat(new IndexerFactory<>(lessThan).isSortedBlockEligible()).isTrue();
        assertThat(new IndexerFactory<>(lessThanGreaterThan).isSortedBlockEligible()).isTrue();
        assertThat(new IndexerFactory<>(equalLessThan).isSortedBlockEligible()).isTrue();
        assertThat(new IndexerFactory<>(equalOnly).isSortedBlockEligible()).isFalse();
        assertThat(new IndexerFactory<>(lessThanContaining).isSortedBlockEligible()).isFalse();
        assertThat(new IndexerFactory<>(neighborhoodsLessThan).isSortedBlockEligible()).isFalse();

        assertThat(new IndexerFactory<>(lessThan).<String> buildIndexer(true)).isInstanceOf(SortedBlockIndexer.class);
        assertThat(new IndexerFactory<>(lessThan).<String> buildIndexer(false)).isInstanceOf(SortedBlockIndexer.class);
        assertThat(new IndexerFactory<>(lessThanGreaterThan).<String> buildIndexer(true))
                .isInstanceOf(SortedBlockIndexer.class);
        assertThat(new IndexerFactory<>(lessThanContaining).<String> buildIndexer(true))
                .isInstanceOf(ComparisonIndexer.class);
        assertThat(new IndexerFactory<>(neighborhoodsLessThan).<String> buildIndexer(false))
                .isInstanceOf(ComparisonIndexer.class);
    }

    @Test
    void factoryBuiltSingleLevelUsesRawKey() {
        var lessThan = (DefaultBiJoiner<TestPerson, TestPerson>) Joiners.lessThan(TestPerson::age);
        var indexer = new IndexerFactory<>(lessThan).<String> buildIndexer(true);
        indexer.put(30, "age30");
        indexer.put(40, "age40");
        var matches = new ArrayList<String>();
        indexer.forEach(35, matches::add);
        assertThat(matches).containsExactly("age30");
        assertThat(indexer.size(41)).isEqualTo(2);
    }

    @Test
    void leftAndRightIndexAreMirrorImages() {
        var random = new Random(11);
        for (var joinerTypes : List.of(List.of(LESS_THAN, GREATER_THAN, LESS_THAN), List.of(GREATER_THAN_OR_EQUAL),
                List.of(LESS_THAN_OR_EQUAL, LESS_THAN))) {
            var left = new Harness(true, joinerTypes);
            var right = new Harness(false, joinerTypes);
            for (var i = 0; i < 200; i++) {
                left.put(random, 20);
                right.put(random, 20);
            }
            for (var leftStored : left.stored) {
                var rightMatches = new ArrayList<Integer>();
                right.indexer.forEach(leftStored.compositeKey, rightMatches::add);
                for (var rightStored : right.stored) {
                    var leftMatches = new ArrayList<Integer>();
                    left.indexer.forEach(rightStored.compositeKey, leftMatches::add);
                    assertThat(leftMatches.contains(leftStored.id)).isEqualTo(rightMatches.contains(rightStored.id));
                }
            }
        }
    }

    /**
     * Keeps a {@link SortedBlockIndexer} and a brute-force list of its contents in sync.
     */
    private static final class Harness {

        private final boolean isLeftBridge;
        private final List<JoinerType> joinerTypes;
        private final SortedBlockIndexer<Integer> indexer;
        private final List<Stored> stored = new ArrayList<>();
        private final int primaryLevel;
        private final boolean descending;
        private int nextId = 0;

        private Harness(boolean isLeftBridge, List<JoinerType> joinerTypes) {
            this.isLeftBridge = isLeftBridge;
            this.joinerTypes = joinerTypes;
            var keyUnpackers = new ArrayList<KeyUnpacker<?>>();
            for (var i = 0; i < joinerTypes.size(); i++) {
                keyUnpackers.add(joinerTypes.size() == 1 ? KeyUnpacker.single() : KeyUnpacker.composite(i));
            }
            this.indexer = new SortedBlockIndexer<>(isLeftBridge, joinerTypes, keyUnpackers);
            var pairStart = SortedBlockIndexer.selectPairStart(joinerTypes);
            this.primaryLevel = Math.max(pairStart, 0);
            var primaryType = joinerTypes.get(primaryLevel);
            var sideType = isLeftBridge ? primaryType : primaryType.flip();
            this.descending = sideType == GREATER_THAN || sideType == GREATER_THAN_OR_EQUAL;
        }

        private int[] randomKeys(Random random, int keyRange) {
            var keys = new int[joinerTypes.size()];
            for (var i = 0; i < keys.length; i++) {
                keys[i] = random.nextInt(keyRange);
            }
            return keys;
        }

        private Object compositeKey(int[] keys) {
            if (keys.length == 1) {
                return keys[0];
            }
            var boxed = new Object[keys.length];
            for (var i = 0; i < keys.length; i++) {
                boxed[i] = keys[i];
            }
            return CompositeKey.ofMany(boxed);
        }

        private void put(Random random, int keyRange) {
            put(randomKeys(random, keyRange));
        }

        private void put(int[] keys) {
            var id = nextId++;
            var compositeKey = compositeKey(keys);
            var entry = indexer.put(compositeKey, id);
            assertThat(entry.element()).isEqualTo(id);
            stored.add(new Stored(id, compositeKey, keys, entry));
        }

        private void remove(int index) {
            var removed = stored.remove(index);
            indexer.remove(removed.compositeKey, removed.entry);
        }

        private int indexOfKeys(int[] keys) {
            for (var i = 0; i < stored.size(); i++) {
                if (Arrays.equals(stored.get(i).keys, keys)) {
                    return i;
                }
            }
            throw new IllegalStateException("No stored keys (%s).".formatted(Arrays.toString(keys)));
        }

        private boolean matches(int[] storedKeys, int[] queryKeys) {
            for (var i = 0; i < storedKeys.length; i++) {
                var joinerType = joinerTypes.get(i);
                // The joiner relation is leftKey OP rightKey; the left index stores the left keys.
                var matches = isLeftBridge ? joinerType.matches(storedKeys[i], queryKeys[i])
                        : joinerType.matches(queryKeys[i], storedKeys[i]);
                if (!matches) {
                    return false;
                }
            }
            return true;
        }

        private void assertQueries(Random random) {
            for (var i = 0; i < 20; i++) {
                assertQuery(randomKeys(random, 110));
            }
        }

        private void assertQuery(int[] queryKeys) {
            var queryCompositeKey = compositeKey(queryKeys);
            var expected = new ArrayList<Integer>();
            for (var candidate : stored) {
                if (matches(candidate.keys, queryKeys)) {
                    expected.add(candidate.id);
                }
            }
            var actual = new ArrayList<Integer>();
            indexer.forEach(queryCompositeKey, actual::add);
            assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
            assertThat(indexer.size(queryCompositeKey)).isEqualTo(expected.size());
            var iterated = new ArrayList<Integer>();
            indexer.iterator(queryCompositeKey).forEachRemaining(iterated::add);
            assertThat(iterated).containsExactlyElementsOf(actual);
            assertPrimaryKeyOrder(actual);
        }

        private void assertPrimaryKeyOrder(List<Integer> actualIds) {
            var previous = descending ? Integer.MAX_VALUE : Integer.MIN_VALUE;
            for (var id : actualIds) {
                var key = stored.stream().filter(s -> s.id == id).findFirst().orElseThrow().keys[primaryLevel];
                if (descending) {
                    assertThat(key).isLessThanOrEqualTo(previous);
                } else {
                    assertThat(key).isGreaterThanOrEqualTo(previous);
                }
                previous = key;
            }
        }

    }

}
