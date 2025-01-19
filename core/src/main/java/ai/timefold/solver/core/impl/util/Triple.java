package ai.timefold.solver.core.impl.util;

import java.util.Objects;

/**
 * An immutable tuple of three values.
 * Two instances {@link Object#equals(Object) are equal} if all three values in the first instance
 * are equal to their counterpart in the other instance.
 *
 * @param <A>
 * @param <B>
 * @param <C>
 */
public record Triple<A, B, C>(A a, B b, C c) {

    public Triple<A, B, C> newIfDifferent(A newA, B newB, C newC) {
        return Objects.equals(a, newA) && Objects.equals(b, newB) && Objects.equals(c, newC) ? this
                : new Triple<>(newA, newB, newC);
    }

}
