package ai.timefold.solver.core.impl.bavet.common.index;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * An {@link EqualKey} of 2 values; replaces {@link ai.timefold.solver.core.impl.util.Pair} in the index,
 * with the same hash code.
 */
@NullMarked
final class BiEqualKey implements EqualKey {

    private final @Nullable Object a;
    private final @Nullable Object b;
    private final int hash;

    BiEqualKey(@Nullable Object a, @Nullable Object b) {
        this.a = a;
        this.b = b;
        var hash = 1;
        hash = 31 * hash + EqualKey.hash(a);
        hash = 31 * hash + EqualKey.hash(b);
        this.hash = hash;
    }

    @Override
    public @Nullable Object get(int index) {
        return switch (index) {
            case 0 -> a;
            case 1 -> b;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof BiEqualKey other && hash == other.hash && EqualKey.equal(a, other.a) && EqualKey.equal(b, other.b);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        return "Pair[key=" + a + ", value=" + b + "]";
    }

}
