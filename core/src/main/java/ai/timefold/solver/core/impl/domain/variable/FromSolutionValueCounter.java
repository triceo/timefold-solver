package ai.timefold.solver.core.impl.domain.variable;

import ai.timefold.solver.core.impl.domain.variable.descriptor.ListVariableDescriptor;

final class FromSolutionValueCounter<Solution_>
        implements ValueCounter<Solution_> {

    private final ListVariableDescriptor<Solution_> sourceVariableDescriptor;
    private int valueCount = 0;

    public FromSolutionValueCounter(ListVariableDescriptor<Solution_> sourceVariableDescriptor) {
        this.sourceVariableDescriptor = sourceVariableDescriptor;
    }

    @Override
    public void resetWorkingSolution(Solution_ workingSolution) {
        valueCount = (int) sourceVariableDescriptor.getValueRangeSize(workingSolution, null);
    }

    @Override
    public void addEntity(Object entity) {
        // No need to do anything.
    }

    @Override
    public void removeEntity(Object entity) {
        // No need to do anything.
    }

    @Override
    public int getCount() {
        return valueCount;
    }
}
