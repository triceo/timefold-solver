package ai.timefold.solver.core.impl.heuristic.selector.value.decorator;

import ai.timefold.solver.core.impl.domain.variable.ListVariableStateSupply;
import ai.timefold.solver.core.impl.heuristic.selector.list.ElementDestinationSelector;
import ai.timefold.solver.core.impl.heuristic.selector.list.LocationInList;
import ai.timefold.solver.core.impl.heuristic.selector.value.EntityIndependentValueSelector;

/**
 * Only selects values from the child value selector that are initialized.
 * This is used for {@link ElementDestinationSelector}’s child value selector during Construction Heuristic phase
 * to filter out values which cannot be used to build a destination {@link LocationInList}.
 */
public final class AssignedListValueSelector<Solution_> extends AbstractInverseEntityFilteringValueSelector<Solution_> {

    public AssignedListValueSelector(EntityIndependentValueSelector<Solution_> childValueSelector) {
        super(childValueSelector);
    }

    @Override
    protected boolean valueFilter(Object value) {
        return rejectUnassignedValues(listVariableStateSupply, value);
    }

    static boolean rejectUnassignedValues(ListVariableStateSupply<?> listVariableStateSupply, Object value) {
        if (listVariableStateSupply.getUnassignedCount() == 0) {
            return false;
        }
        return !listVariableStateSupply.isAssigned(value);
    }

    @Override
    public String toString() {
        return "Assigned(" + childValueSelector + ")";
    }
}
