package ai.timefold.solver.core.impl.bavet.common.index;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * An {@link EqualKey} of 4 values; replaces {@link ai.timefold.solver.core.impl.util.Quadruple} in the index,
 * with the same hash code.
 */
@NullMarked
final class QuadEqualKey implements EqualKey {

    private final @Nullable Object a;
    private final @Nullable Object b;
    private final @Nullable Object c;
    private final @Nullable Object d;
    private final int hash;

    QuadEqualKey(@Nullable Object a, @Nullable Object b, @Nullable Object c, @Nullable Object d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        var hash = 1;
        hash = 31 * hash + EqualKey.hash(a);
        hash = 31 * hash + EqualKey.hash(b);
        hash = 31 * hash + EqualKey.hash(c);
        hash = 31 * hash + EqualKey.hash(d);
        this.hash = hash;
    }

    @Override
    public @Nullable Object get(int index) {
        return switch (index) {
            case 0 -> a;
            case 1 -> b;
            case 2 -> c;
            case 3 -> d;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }

    @Override
    public int size() {
        return 4;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof QuadEqualKey other && hash == other.hash && EqualKey.equal(a, other.a) && EqualKey.equal(b, other.b)
                && EqualKey.equal(c, other.c) && EqualKey.equal(d, other.d);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        return "Quadruple[a=" + a + ", b=" + b + ", c=" + c + ", d=" + d + "]";
    }

}
