package ai.timefold.solver.core.impl.domain.valuerange.descriptor;

import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.valuerange.ValueRange;

/**
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public sealed interface FromSolutionPropertyValueRangeDescriptor<Solution_>
        extends ValueRangeDescriptor<Solution_>
        permits CompositePropertyValueRangeDescriptor, FromSolutionPropertyValueRangeDescriptorImpl {

    /**
     * @param solution never null
     * @return never null
     */
    ValueRange<?> extractValueRange(Solution_ solution);

    /**
     * @param solution never null
     * @return never null
     */
    long extractValueRangeSize(Solution_ solution);

}
