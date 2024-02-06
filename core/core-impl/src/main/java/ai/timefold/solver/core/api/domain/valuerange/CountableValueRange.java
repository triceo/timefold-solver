package ai.timefold.solver.core.api.domain.valuerange;

import java.util.Iterator;

import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

/**
 * A {@link ValueRange} that is ending. Therefore, it has a discrete (as in non-continuous) range.
 *
 * @see ValueRangeFactory
 * @see ValueRange
 */
public interface CountableValueRange<T> extends ValueRange<T> {

    /**
     * Used by uniform random selection in a composite CountableValueRange,
     * or one which includes nulls.
     *
     * @return the exact number of elements generated by this {@link CountableValueRange}, always {@code >= 0}
     */
    long getSize();

    /**
     * Used by uniform random selection in a composite CountableValueRange,
     * or one which includes nulls.
     *
     * @param index always {@code <} {@link #getSize()}
     * @return sometimes null (if {@link PlanningVariable#allowsUnassigned()} is true)
     */
    T get(long index);

    /**
     * Select the elements in original (natural) order.
     *
     * @return never null
     */
    Iterator<T> createOriginalIterator();

}
