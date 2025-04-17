package ai.timefold.solver.core.impl.util;

import java.util.Objects;

public final class ObjectUtils {

    /**
     * Has two primary uses:
     *
     * <ul>
     * <li>When a record is used as a key in a map, it may be beneficial to override its hashCode.
     * The default implementation does a lot of magic to access the record's fields,
     * and we can benefit from circumventing it.</li>
     * <li>When {@link Objects#hash(Object...)} would be called instead.
     * That method will create a new array of objects on every invocation,
     * leading to significant GC pressure.</li>
     * </ul>
     *
     * There is no need to use this if:
     *
     * <ul>
     * <li>The class in question is not used as a key in a map,
     * or in another situation where the hash is heavily exposed.</li>
     * <li>The use is not on the hot path.</li>
     * </ul>
     *
     * In both of these cases, using this method would be considered premature optimization,
     * as the use of default hashCode doesn't lead to any measurable problems.
     */
    public static int hashCode(Object a, Object b) {
        return 31 * Objects.hashCode(a) + Objects.hashCode(b);
    }

    /**
     * As defined by {@link #hashCode(Object, Object)}, but for 3 objects.
     */
    public static int hashCode(Object a, Object b, Object c) {
        return 31 * hashCode(a, b) + Objects.hashCode(c);
    }

    /**
     * As defined by {@link #hashCode(Object, Object)}, but for 4 objects.
     */
    public static int hashCode(Object a, Object b, Object c, Object d) {
        return 31 * hashCode(a, b, c) + Objects.hashCode(d);
    }

    private ObjectUtils() {
        // No external instances.
    }

}
