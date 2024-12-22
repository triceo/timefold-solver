package ai.timefold.solver.core.impl.util;

import java.util.Objects;

/**
 * An immutable key-value tuple.
 * Two instances {@link Object#equals(Object) are equal} if both values in the first instance
 * are equal to their counterpart in the other instance.
 *
 * @param <Key_>
 * @param <Value_>
 */
public record Pair<Key_, Value_>(Key_ key, Value_ value) {

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair<?, ?> pair)) {
            return false;
        }
        return Objects.equals(key, pair.key) && Objects.equals(value, pair.value);
    }

    @Override
    public int hashCode() {
        var hashCode = 0;
        hashCode = 31 * hashCode + (key == null ? 0 : key.hashCode());
        hashCode = 31 * hashCode + (value == null ? 0 : value.hashCode());
        return hashCode;
    }
}
