package ai.timefold.solver.core.impl.domain.valuerange.descriptor;

import java.util.List;

import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.valuerange.ValueRange;
import ai.timefold.solver.core.impl.domain.common.accessor.MemberAccessor;
import ai.timefold.solver.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;

/**
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public sealed interface ValueRangeDescriptor<Solution_>
        permits AbstractValueRangeDescriptor, FromEntityValueRangeDescriptor, FromSolutionValueRangeDescriptor {

    static <Solution_> FromSolutionValueRangeDescriptor<Solution_> fromSolution(
            GenuineVariableDescriptor<Solution_> variableDescriptor, MemberAccessor memberAccessor,
            boolean addNullInValueRange) {
        return new FromSolutionValueRangeDescriptorImpl<>(variableDescriptor, memberAccessor, addNullInValueRange);
    }

    static <Solution_> FromEntityValueRangeDescriptor<Solution_> fromEntity(
            GenuineVariableDescriptor<Solution_> variableDescriptor, MemberAccessor memberAccessor,
            boolean addNullInValueRange) {
        return new FromEntityValueRangeDescriptorImpl<>(variableDescriptor, memberAccessor, addNullInValueRange);
    }

    static <Solution_> ValueRangeDescriptor<Solution_> compose(GenuineVariableDescriptor<Solution_> variableDescriptor,
            boolean addNullInValueRange, List<ValueRangeDescriptor<Solution_>> childValueRangeDescriptorList) {
        return new CompositeValueRangeDescriptor<>(variableDescriptor, addNullInValueRange, childValueRangeDescriptorList);
    }

    /**
     * @return never null
     */
    GenuineVariableDescriptor<Solution_> getVariableDescriptor();

    /**
     * @return true if the {@link ValueRange} is countable
     *         (for example a double value range between 1.2 and 1.4 is not countable)
     */
    boolean isCountable();

    /**
     * @return true if the {@link ValueRange} might contain a planning entity instance
     *         (not necessarily of the same entity class as this entity class of this descriptor.
     */
    boolean mightContainEntity();

}
