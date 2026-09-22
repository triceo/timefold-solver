package ai.timefold.solver.core.impl.bavet.common.index;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * An {@link EqualKey} of 3 values; replaces {@link ai.timefold.solver.core.impl.util.Triple} in the index,
 * with the same hash code.
 */
@NullMarked
final class TriEqualKey implements EqualKey {

    private final @Nullable Object a;
    private final @Nullable Object b;
    private final @Nullable Object c;
    private final int hash;

    TriEqualKey(@Nullable Object a, @Nullable Object b, @Nullable Object c) {
        this.a = a;
        this.b = b;
        this.c = c;
        var hash = 1;
        hash = 31 * hash + EqualKey.hash(a);
        hash = 31 * hash + EqualKey.hash(b);
        hash = 31 * hash + EqualKey.hash(c);
        this.hash = hash;
    }

    @Override
    public @Nullable Object get(int index) {
        return switch (index) {
            case 0 -> a;
            case 1 -> b;
            case 2 -> c;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }

    @Override
    public int size() {
        return 3;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof TriEqualKey other && hash == other.hash && EqualKey.equal(a, other.a) && EqualKey.equal(b, other.b)
                && EqualKey.equal(c, other.c);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        return "Triple[a=" + a + ", b=" + b + ", c=" + c + "]";
    }

}
