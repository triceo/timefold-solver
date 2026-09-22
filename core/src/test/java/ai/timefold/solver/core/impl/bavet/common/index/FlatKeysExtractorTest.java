package ai.timefold.solver.core.impl.bavet.common.index;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;

import ai.timefold.solver.core.api.score.stream.Joiners;
import ai.timefold.solver.core.impl.bavet.bi.joiner.DefaultBiJoiner;
import ai.timefold.solver.core.impl.bavet.common.tuple.BiTuple;
import ai.timefold.solver.core.impl.bavet.common.tuple.UniTuple;

import org.junit.jupiter.api.Test;

class FlatKeysExtractorTest {

    @Test
    void keyShapes() {
        var a = "a";
        var b = 1;
        var c = 2L;
        // No equal prefix, or a single equal joiner: level 0 is a raw value.
        assertThat(uniKey(0, a, b)).isEqualTo(CompositeKey.of(a, b));
        assertThat(uniKey(1, a, b)).isEqualTo(CompositeKey.of(a, b));
        assertThat(uniKey(0, a, b, c)).isEqualTo(CompositeKey.ofMany(a, b, c));
        // Pure equal: the key is the level 0 key itself.
        assertThat(uniKey(2, a, b)).isEqualTo(new BiEqualKey(a, b));
        assertThat(uniKey(3, a, b, c)).isEqualTo(new TriEqualKey(a, b, c));
        assertThat(uniKey(4, a, b, c, "d")).isEqualTo(new QuadEqualKey(a, b, c, "d"));
        assertThat(uniKey(5, a, b, c, "d", "e")).isEqualTo(new IndexerKey(a, b, c, "d", "e"));
        // Equal prefix and suffix.
        assertThat(uniKey(2, a, b, c)).isEqualTo(CompositeKey.of(new BiEqualKey(a, b), c));
        assertThat(uniKey(2, a, b, c, "d")).isEqualTo(CompositeKey.ofMany(new BiEqualKey(a, b), c, "d"));
        assertThat(uniKey(5, a, b, c, "d", "e", "f"))
                .isEqualTo(CompositeKey.of(new IndexerKey(a, b, c, "d", "e"), "f"));
    }

    @Test
    void unchangedValuesReturnPreviousKey() {
        for (var joinerCount = 2; joinerCount <= 6; joinerCount++) {
            for (var equalPrefixLength = 0; equalPrefixLength <= joinerCount; equalPrefixLength++) {
                var values = values(joinerCount);
                var mappings = new CountingMappings(values);
                var extractor = AbstractFlatKeysExtractor.uni(mappings.uni(), equalPrefixLength);
                var tuple = UniTuple.of("fact", 0);
                var previousKey = extractor.apply(tuple);
                // Equal, but not the same instances.
                for (var i = 0; i < values.length; i++) {
                    values[i] = new String((String) values[i]);
                }
                mappings.reset();
                assertThat(extractor.apply(tuple, previousKey)).isSameAs(previousKey);
                assertThat(mappings.callCounts).containsOnly(1);
            }
        }
    }

    @Test
    void changedValueReturnsNewKey() {
        for (var joinerCount = 2; joinerCount <= 6; joinerCount++) {
            for (var equalPrefixLength = 0; equalPrefixLength <= joinerCount; equalPrefixLength++) {
                for (var changedIndex = 0; changedIndex < joinerCount; changedIndex++) {
                    var values = values(joinerCount);
                    var mappings = new CountingMappings(values);
                    var extractor = AbstractFlatKeysExtractor.uni(mappings.uni(), equalPrefixLength);
                    var tuple = UniTuple.of("fact", 0);
                    var previousKey = extractor.apply(tuple);
                    values[changedIndex] = "changed";
                    mappings.reset();
                    var newKey = extractor.apply(tuple, previousKey);
                    assertThat(mappings.callCounts).containsOnly(1);
                    assertThat(newKey)
                            .isNotSameAs(previousKey)
                            .isNotEqualTo(previousKey)
                            .isEqualTo(extractor.apply(tuple));
                }
            }
        }
    }

    @Test
    void nullValues() {
        for (var joinerCount = 2; joinerCount <= 6; joinerCount++) {
            for (var equalPrefixLength = 0; equalPrefixLength <= joinerCount; equalPrefixLength++) {
                var values = new Object[joinerCount];
                var mappings = new CountingMappings(values);
                var extractor = AbstractFlatKeysExtractor.uni(mappings.uni(), equalPrefixLength);
                var tuple = UniTuple.of("fact", 0);
                var previousKey = extractor.apply(tuple);
                assertThat(extractor.apply(tuple, previousKey)).isSameAs(previousKey);
                values[joinerCount - 1] = "a";
                var newKey = extractor.apply(tuple, previousKey);
                assertThat(newKey).isNotEqualTo(previousKey).isEqualTo(extractor.apply(tuple));
                values[joinerCount - 1] = null;
                assertThat(extractor.apply(tuple, newKey)).isEqualTo(previousKey);
            }
        }
    }

    @Test
    void biTuple() {
        BiFunction<String, String, Object>[] mappings = new BiFunction[] {
                (BiFunction<String, String, Object>) (x, y) -> x,
                (BiFunction<String, String, Object>) (x, y) -> y,
                (BiFunction<String, String, Object>) (x, y) -> x + y };
        var extractor = AbstractFlatKeysExtractor.bi(mappings, 2);
        var tuple = BiTuple.of("a", "b", 0);
        var key = extractor.apply(tuple);
        assertThat(key).isEqualTo(CompositeKey.of(new BiEqualKey("a", "b"), "ab"));
        assertThat(extractor.apply(tuple, key)).isSameAs(key);
        tuple.setB("c");
        assertThat(extractor.apply(tuple, key)).isEqualTo(CompositeKey.of(new BiEqualKey("a", "c"), "ac"));
    }

    @Test
    void factAndTupleExtractorsBuildEqualKeys() {
        // Both sides query the same index, so the fact-based (Neighborhoods) and tuple-based keys must be equal.
        var joiner = (DefaultBiJoiner<String, String>) Joiners.<String, Integer> equal(String::length)
                .and(Joiners.<String, Character> equal(s -> s.charAt(0)))
                .and(Joiners.<String, String> lessThan(s -> s));
        var indexerFactory = new IndexerFactory<>(joiner);
        var factKey = indexerFactory.<String> buildUniLeftFactKeysExtractor().apply("abc");
        var tupleKey = indexerFactory.<String> buildUniLeftKeysExtractor().apply(UniTuple.of("abc", 0));
        var rightKey = indexerFactory.buildRightKeysExtractor().apply(UniTuple.of("abc", 0));
        assertThat(factKey)
                .isEqualTo(CompositeKey.of(new BiEqualKey(3, 'a'), "abc"))
                .isEqualTo(tupleKey)
                .isEqualTo(rightKey);
    }

    private static Object uniKey(int equalPrefixLength, Object... values) {
        var mappings = new CountingMappings(values);
        return AbstractFlatKeysExtractor.uni(mappings.uni(), equalPrefixLength).apply(UniTuple.of("fact", 0));
    }

    private static Object[] values(int count) {
        var values = new Object[count];
        for (var i = 0; i < count; i++) {
            values[i] = "value" + i;
        }
        return values;
    }

    private static final class CountingMappings {

        private final Object[] values;
        private final int[] callCounts;

        CountingMappings(Object[] values) {
            this.values = values;
            this.callCounts = new int[values.length];
        }

        @SuppressWarnings("unchecked")
        Function<String, Object>[] uni() {
            Function<String, Object>[] mappings = new Function[values.length];
            for (var i = 0; i < values.length; i++) {
                var index = i;
                mappings[i] = fact -> {
                    callCounts[index]++;
                    return values[index];
                };
            }
            return mappings;
        }

        void reset() {
            Arrays.fill(callCounts, 0);
        }

    }

}
