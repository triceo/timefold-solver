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

    public Pair<Key_, Value_> newIfDifferent(Key_ newA, Value_ newB) {
        return Objects.equals(key, newA) && Objects.equals(value, newB) ? this : new Pair<>(newA, newB);
    }

}
