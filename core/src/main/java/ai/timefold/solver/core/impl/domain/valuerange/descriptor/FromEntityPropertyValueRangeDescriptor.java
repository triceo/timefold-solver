package ai.timefold.solver.core.impl.domain.valuerange.descriptor;

import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.valuerange.ValueRange;

/**
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public sealed interface FromEntityPropertyValueRangeDescriptor<Solution_>
        extends ValueRangeDescriptor<Solution_>
        permits FromEntityPropertyValueRangeDescriptorImpl {

    /**
     * @param solution never null
     * @param entity never null. To avoid this parameter,
     *        use {@link FromSolutionPropertyValueRangeDescriptor#extractValueRange} instead.
     * @return never null
     */
    ValueRange<?> extractValueRange(Solution_ solution, Object entity);

    /**
     * @param solution never null
     * @param entity never null. To avoid this parameter,
     *        use {@link FromSolutionPropertyValueRangeDescriptor#extractValueRangeSize} instead.
     * @return never null
     * @throws UnsupportedOperationException if {@link #isCountable()} returns false
     */
    long extractValueRangeSize(Solution_ solution, Object entity);

}
