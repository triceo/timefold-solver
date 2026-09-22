package ai.timefold.solver.core.impl.bavet.common.index;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import ai.timefold.solver.core.api.function.QuadFunction;
import ai.timefold.solver.core.api.function.TriFunction;
import ai.timefold.solver.core.impl.bavet.common.index.IndexerFactory.BiKeysExtractor;
import ai.timefold.solver.core.impl.bavet.common.index.IndexerFactory.KeysExtractor;
import ai.timefold.solver.core.impl.bavet.common.index.IndexerFactory.QuadKeysExtractor;
import ai.timefold.solver.core.impl.bavet.common.index.IndexerFactory.TriKeysExtractor;
import ai.timefold.solver.core.impl.bavet.common.index.IndexerFactory.UniKeysExtractor;
import ai.timefold.solver.core.impl.bavet.common.tuple.BiTuple;
import ai.timefold.solver.core.impl.bavet.common.tuple.QuadTuple;
import ai.timefold.solver.core.impl.bavet.common.tuple.TriTuple;
import ai.timefold.solver.core.impl.bavet.common.tuple.Tuple;
import ai.timefold.solver.core.impl.bavet.common.tuple.UniTuple;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Extracts the composite key of a joiner with at least two joiners,
 * from a flat list of mappings (one per joiner).
 * Builds keys of the same shape as the fact-based extractors in {@link IndexerFactory}:
 * level 0 is the merged equal run (a raw value, or an {@link EqualKey}),
 * every other level is a raw value,
 * and the levels are combined into a {@link CompositeKey}.
 * <p>
 * {@link #apply(Tuple, Object)} returns the previous key itself when no value changed,
 * which saves the allocation of a new key on the update path.
 *
 * @param <Tuple_>
 */
@NullMarked
abstract sealed class AbstractFlatKeysExtractor<Tuple_ extends Tuple> implements KeysExtractor<Tuple_> {

    static <A> UniKeysExtractor<A> uni(Function<A, Object>[] mappings, int equalPrefixLength) {
        return new Uni<>(mappings, equalPrefixLength);
    }

    static <A, B> BiKeysExtractor<A, B> bi(BiFunction<A, B, Object>[] mappings, int equalPrefixLength) {
        return new Bi<>(mappings, equalPrefixLength);
    }

    static <A, B, C> TriKeysExtractor<A, B, C> tri(TriFunction<A, B, C, Object>[] mappings, int equalPrefixLength) {
        return new Tri<>(mappings, equalPrefixLength);
    }

    static <A, B, C, D> QuadKeysExtractor<A, B, C, D> quad(QuadFunction<A, B, C, D, Object>[] mappings,
            int equalPrefixLength) {
        return new Quad<>(mappings, equalPrefixLength);
    }

    private final int valueCount;
    // How many values the merged equal run of level 0 holds; always at least 1.
    private final int firstLevelLength;
    private final int levelCount;
    // Per value: whether the key compares it with Arrays.deepEquals (true) or with equals (false).
    private final boolean[] deepComparedValues;

    private AbstractFlatKeysExtractor(int valueCount, int equalPrefixLength) {
        if (valueCount < 2) {
            throw new IllegalArgumentException("Impossible state: the valueCount (%d) must be at least 2."
                    .formatted(valueCount));
        }
        this.valueCount = valueCount;
        this.firstLevelLength = Math.max(equalPrefixLength, 1);
        this.levelCount = valueCount - firstLevelLength + 1;
        this.deepComparedValues = new boolean[valueCount];
        for (var i = 0; i < valueCount; i++) {
            deepComparedValues[i] = i < firstLevelLength && firstLevelLength > 1
                    ? firstLevelLength > 4 // Only IndexerKey compares deeply; BiEqualKey and friends do not.
                    : levelCount > 2; // Only MegaCompositeKey compares deeply; BiCompositeKey does not.
        }
    }

    abstract @Nullable Object value(Tuple_ tuple, int index);

    @Override
    public final Object apply(Tuple_ tuple) {
        var firstLevel = switch (firstLevelLength) {
            case 1 -> value(tuple, 0);
            case 2 -> new BiEqualKey(value(tuple, 0), value(tuple, 1));
            case 3 -> new TriEqualKey(value(tuple, 0), value(tuple, 1), value(tuple, 2));
            case 4 -> new QuadEqualKey(value(tuple, 0), value(tuple, 1), value(tuple, 2), value(tuple, 3));
            default -> {
                var properties = new Object[firstLevelLength];
                for (var i = 0; i < firstLevelLength; i++) {
                    properties[i] = value(tuple, i);
                }
                yield new IndexerKey(properties);
            }
        };
        return switch (levelCount) {
            case 1 -> firstLevel;
            case 2 -> CompositeKey.of(firstLevel, value(tuple, firstLevelLength));
            default -> {
                var levels = new Object[levelCount];
                levels[0] = firstLevel;
                for (var level = 1; level < levelCount; level++) {
                    levels[level] = value(tuple, firstLevelLength + level - 1);
                }
                yield CompositeKey.ofMany(levels);
            }
        };
    }

    @Override
    public final Object apply(Tuple_ tuple, Object previousKey) {
        for (var i = 0; i < valueCount; i++) {
            var value = value(tuple, i);
            var previousValue = component(previousKey, i);
            var same = deepComparedValues[i] ? Objects.deepEquals(value, previousValue)
                    : value == previousValue || (value != null && value.equals(previousValue));
            if (!same) {
                return rebuild(tuple, previousKey, i, value);
            }
        }
        return previousKey;
    }

    /**
     * Values before {@code changedIndex} are equal to the previous key's, so they are taken from it;
     * this way, every mapping is called exactly once per update.
     */
    private Object rebuild(Tuple_ tuple, Object previousKey, int changedIndex, @Nullable Object changedValue) {
        var values = new Object[valueCount];
        for (var i = 0; i < changedIndex; i++) {
            values[i] = component(previousKey, i);
        }
        values[changedIndex] = changedValue;
        for (var i = changedIndex + 1; i < valueCount; i++) {
            values[i] = value(tuple, i);
        }
        var firstLevel = switch (firstLevelLength) {
            case 1 -> values[0];
            case 2 -> new BiEqualKey(values[0], values[1]);
            case 3 -> new TriEqualKey(values[0], values[1], values[2]);
            case 4 -> new QuadEqualKey(values[0], values[1], values[2], values[3]);
            default -> {
                var properties = new Object[firstLevelLength];
                System.arraycopy(values, 0, properties, 0, firstLevelLength);
                yield new IndexerKey(properties);
            }
        };
        return switch (levelCount) {
            case 1 -> firstLevel;
            case 2 -> CompositeKey.of(firstLevel, values[firstLevelLength]);
            default -> {
                var levels = new Object[levelCount];
                levels[0] = firstLevel;
                System.arraycopy(values, firstLevelLength, levels, 1, levelCount - 1);
                yield CompositeKey.ofMany(levels);
            }
        };
    }

    /**
     * The reverse of {@link #apply(Tuple)}: reads the value at the given flat index from a key it built.
     */
    @Nullable
    Object component(Object key, int index) {
        if (levelCount == 1) {
            return ((EqualKey) key).get(index);
        }
        var compositeKey = (CompositeKey) key;
        if (index >= firstLevelLength) {
            return compositeKey.get(index - firstLevelLength + 1);
        }
        Object firstLevel = compositeKey.get(0);
        return firstLevelLength == 1 ? firstLevel : ((EqualKey) firstLevel).get(index);
    }

    private static final class Uni<A> extends AbstractFlatKeysExtractor<UniTuple<A>> implements UniKeysExtractor<A> {

        private final Function<A, Object>[] mappings;

        private Uni(Function<A, Object>[] mappings, int equalPrefixLength) {
            super(mappings.length, equalPrefixLength);
            this.mappings = mappings;
        }

        @Override
        @Nullable
        Object value(UniTuple<A> tuple, int index) {
            return mappings[index].apply(tuple.getA());
        }

    }

    private static final class Bi<A, B> extends AbstractFlatKeysExtractor<BiTuple<A, B>> implements BiKeysExtractor<A, B> {

        private final BiFunction<A, B, Object>[] mappings;

        private Bi(BiFunction<A, B, Object>[] mappings, int equalPrefixLength) {
            super(mappings.length, equalPrefixLength);
            this.mappings = mappings;
        }

        @Override
        @Nullable
        Object value(BiTuple<A, B> tuple, int index) {
            return mappings[index].apply(tuple.getA(), tuple.getB());
        }

    }

    private static final class Tri<A, B, C> extends AbstractFlatKeysExtractor<TriTuple<A, B, C>>
            implements TriKeysExtractor<A, B, C> {

        private final TriFunction<A, B, C, Object>[] mappings;

        private Tri(TriFunction<A, B, C, Object>[] mappings, int equalPrefixLength) {
            super(mappings.length, equalPrefixLength);
            this.mappings = mappings;
        }

        @Override
        @Nullable
        Object value(TriTuple<A, B, C> tuple, int index) {
            return mappings[index].apply(tuple.getA(), tuple.getB(), tuple.getC());
        }

    }

    private static final class Quad<A, B, C, D> extends AbstractFlatKeysExtractor<QuadTuple<A, B, C, D>>
            implements QuadKeysExtractor<A, B, C, D> {

        private final QuadFunction<A, B, C, D, Object>[] mappings;

        private Quad(QuadFunction<A, B, C, D, Object>[] mappings, int equalPrefixLength) {
            super(mappings.length, equalPrefixLength);
            this.mappings = mappings;
        }

        @Override
        @Nullable
        Object value(QuadTuple<A, B, C, D> tuple, int index) {
            return mappings[index].apply(tuple.getA(), tuple.getB(), tuple.getC(), tuple.getD());
        }

    }

}
