package ai.timefold.solver.core.impl.domain.variable.supply;

import java.util.Objects;

import ai.timefold.solver.core.impl.domain.variable.descriptor.VariableDescriptor;
import ai.timefold.solver.core.impl.util.ObjectUtils;

/**
 * Some {@link Demand} implementation classes are defined by their {@link VariableDescriptor} and nothing else.
 * However, they still must not equal (and therefore have the same {@link #hashCode()})
 * as other {@link Demand} implementation classes defined by the same {@link VariableDescriptor}.
 * This helper abstraction exists so that this logic can be shared across all such {@link Demand} implementations.
 *
 * @param <Solution_>
 * @param <Supply_>
 */
public abstract class AbstractVariableDescriptorBasedDemand<Solution_, Supply_ extends Supply>
        implements Demand<Supply_> {

    protected final VariableDescriptor<Solution_> variableDescriptor;

    protected AbstractVariableDescriptorBasedDemand(VariableDescriptor<Solution_> variableDescriptor) {
        this.variableDescriptor = Objects.requireNonNull(variableDescriptor);
    }

    @Override
    public final boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;
        AbstractVariableDescriptorBasedDemand<?, ?> that = (AbstractVariableDescriptorBasedDemand<?, ?>) other;
        return Objects.equals(variableDescriptor, that.variableDescriptor);
    }

    @Override
    public final int hashCode() {
        return ObjectUtils.hashCode(getClass(), variableDescriptor);
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName() + "(" + variableDescriptor.getSimpleEntityAndVariableName() + ")";
    }

}
