package ai.timefold.solver.core.impl.bavet.common.index;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * The index key of a merged run of equal joiners (level 0 of a {@link CompositeKey}), with more than one value.
 * Implementations cache their hash code, as they are hashed on every index lookup.
 * The hash is computed with the same formula as {@link ai.timefold.solver.core.impl.util.Pair} and friends,
 * so that hash-based iteration order does not depend on which key class is used.
 */
@NullMarked
sealed interface EqualKey permits BiEqualKey, TriEqualKey, QuadEqualKey, IndexerKey {

    @Nullable
    Object get(int index);

    int size();

    static int hash(@Nullable Object value) {
        // We do not use Objects.hashCode() due to https://bugs.openjdk.org/browse/JDK-8015417.
        return value == null ? 0 : value.hashCode();
    }

    static boolean equal(@Nullable Object left, @Nullable Object right) {
        // We do not use Objects.equals(...) due to https://bugs.openjdk.org/browse/JDK-8015417.
        return left == right || (left != null && left.equals(right));
    }

}
