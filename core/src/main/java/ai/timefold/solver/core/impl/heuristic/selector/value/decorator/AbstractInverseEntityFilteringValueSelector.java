package ai.timefold.solver.core.impl.heuristic.selector.value.decorator;

import java.util.Iterator;
import java.util.Objects;

import ai.timefold.solver.core.impl.domain.variable.ListVariableStateSupply;
import ai.timefold.solver.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import ai.timefold.solver.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import ai.timefold.solver.core.impl.heuristic.selector.AbstractDemandEnabledSelector;
import ai.timefold.solver.core.impl.heuristic.selector.value.EntityIndependentValueSelector;
import ai.timefold.solver.core.impl.phase.scope.AbstractPhaseScope;
import ai.timefold.solver.core.impl.util.SkippingIterator;

/**
 * Filters planning values based on their assigned status. The assigned status is determined using the inverse supply.
 * If the inverse entity is not null, the value is assigned, otherwise it is unassigned.
 * A subclass must implement the {@link #valueFilter(Object)} to decide whether assigned or unassigned values will be selected.
 * <p>
 * Does implement {@link EntityIndependentValueSelector} because the question whether a value is assigned or not does not depend
 * on a specific entity.
 */
abstract class AbstractInverseEntityFilteringValueSelector<Solution_>
        extends AbstractDemandEnabledSelector<Solution_>
        implements EntityIndependentValueSelector<Solution_> {

    protected final EntityIndependentValueSelector<Solution_> childValueSelector;

    protected ListVariableStateSupply<Solution_> listVariableStateSupply;

    protected AbstractInverseEntityFilteringValueSelector(EntityIndependentValueSelector<Solution_> childValueSelector) {
        if (childValueSelector.isNeverEnding()) {
            throw new IllegalArgumentException("The selector (" + this
                    + ") has a childValueSelector (" + childValueSelector
                    + ") with neverEnding (" + childValueSelector.isNeverEnding() + ").\n"
                    + "This is not allowed because " + AbstractInverseEntityFilteringValueSelector.class.getSimpleName()
                    + " cannot decorate a never-ending child value selector.\n"
                    + "This could be a result of using random selection order (which is often the default).");
        }
        this.childValueSelector = childValueSelector;
        phaseLifecycleSupport.addEventListener(childValueSelector);
    }

    protected abstract boolean valueFilter(Object value);

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void phaseStarted(AbstractPhaseScope<Solution_> phaseScope) {
        super.phaseStarted(phaseScope);
        ListVariableDescriptor<Solution_> variableDescriptor =
                (ListVariableDescriptor<Solution_>) childValueSelector.getVariableDescriptor();
        listVariableStateSupply = phaseScope.getScoreDirector().getSupplyManager()
                .demand(variableDescriptor.getStateDemand());
    }

    @Override
    public void phaseEnded(AbstractPhaseScope<Solution_> phaseScope) {
        super.phaseEnded(phaseScope);
        listVariableStateSupply = null;
    }

    @Override
    public GenuineVariableDescriptor<Solution_> getVariableDescriptor() {
        return childValueSelector.getVariableDescriptor();
    }

    @Override
    public boolean isCountable() {
        // Because !neverEnding => countable.
        return true;
    }

    @Override
    public boolean isNeverEnding() {
        // Because the childValueSelector is not never-ending.
        return false;
    }

    @Override
    public long getSize(Object entity) {
        var count = 0;
        var iterator = iterator(entity);
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }
        return count;
    }

    @Override
    public long getSize() {
        var count = 0;
        for (var object : this) {
            count++;
        }
        return count;
    }

    @Override
    public Iterator<Object> iterator(Object entity) {
        return iterator();
    }

    @Override
    public Iterator<Object> iterator() {
        return new SkippingIterator<>(childValueSelector.iterator(), this::valueFilter);
    }

    @Override
    public Iterator<Object> endingIterator(Object entity) {
        return iterator();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;
        AbstractInverseEntityFilteringValueSelector<?> that = (AbstractInverseEntityFilteringValueSelector<?>) other;
        return Objects.equals(childValueSelector, that.childValueSelector);
    }

    @Override
    public int hashCode() {
        return Objects.hash(childValueSelector);
    }

}
