package ai.timefold.solver.core.impl.util;

import java.util.Objects;

/**
 * An immutable tuple of four values.
 * Two instances {@link Object#equals(Object) are equal} if all four values in the first instance
 * are equal to their counterpart in the other instance.
 *
 * @param <A>
 * @param <B>
 * @param <C>
 * @param <D>
 */
public record Quadruple<A, B, C, D>(A a, B b, C c, D d) {

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Quadruple<?, ?, ?, ?> quadruple)) {
            return false;
        }
        return Objects.equals(a, quadruple.a) && Objects.equals(b, quadruple.b) && Objects.equals(c, quadruple.c) && Objects.equals(d, quadruple.d);
    }

    @Override
    public int hashCode() {
        var hashCode = 0;
        hashCode = 31 * hashCode + (a == null ? 0 : a.hashCode());
        hashCode = 31 * hashCode + (b == null ? 0 : b.hashCode());
        hashCode = 31 * hashCode + (c == null ? 0 : c.hashCode());
        hashCode = 31 * hashCode + (d == null ? 0 : d.hashCode());
        return hashCode;
    }

}
