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
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Triple<?, ?, ?> triple)) {
            return false;
        }
        return Objects.equals(a, triple.a) && Objects.equals(b, triple.b) && Objects.equals(c, triple.c);
    }

    @Override
    public int hashCode() {
        var hashCode = 0;
        hashCode = 31 * hashCode + (a == null ? 0 : a.hashCode());
        hashCode = 31 * hashCode + (b == null ? 0 : b.hashCode());
        hashCode = 31 * hashCode + (c == null ? 0 : c.hashCode());
        return hashCode;
    }
}
