package ai.timefold.solver.core.impl.bavet.common.index;

import java.util.Arrays;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * An {@link EqualKey} of more than 4 values; smaller runs use {@link BiEqualKey}, {@link TriEqualKey}, ...
 * Overrides {@link Object#equals(Object)} and {@link Object#hashCode()} as it references external object.
 */
@NullMarked
final class IndexerKey implements EqualKey {

    private final @Nullable Object[] properties;
    private final int hash;

    IndexerKey(@Nullable Object... properties) {
        this.properties = properties;
        this.hash = Arrays.deepHashCode(properties);
    }

    @Override
    public @Nullable Object get(int index) {
        return properties[index];
    }

    @Override
    public int size() {
        return properties.length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof IndexerKey other && hash == other.hash && Arrays.deepEquals(properties, other.properties);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        return Arrays.toString(properties);
    }

}
