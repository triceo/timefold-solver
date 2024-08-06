package ai.timefold.solver.core.impl.domain.valuerange.descriptor;

import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.valuerange.ValueRange;
import ai.timefold.solver.core.impl.domain.common.accessor.MemberAccessor;
import ai.timefold.solver.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;

/**
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
final class FromEntityValueRangeDescriptorImpl<Solution_>
        extends AbstractFromPropertyValueRangeDescriptor<Solution_>
        implements FromEntityValueRangeDescriptor<Solution_> {

    public FromEntityValueRangeDescriptorImpl(GenuineVariableDescriptor<Solution_> variableDescriptor,
            MemberAccessor memberAccessor, boolean addNullInValueRange) {
        super(variableDescriptor, memberAccessor, addNullInValueRange);
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public ValueRange<?> extractValueRange(Solution_ solution, Object entity) {
        return readValueRange(entity);
    }

    @Override
    public long extractValueRangeSize(Solution_ solution, Object entity) {
        return readValueRangeSize(entity);
    }

}
