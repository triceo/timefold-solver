package ai.timefold.solver.core.impl.heuristic.selector.value;

import ai.timefold.solver.core.impl.domain.variable.descriptor.ListVariableDescriptor;

public sealed interface ValueCounter<Solution_>
        permits FromEntityPropertyValueCounter, FromSolutionPropertyValueCounter {

    static <Solution_> ValueCounter<Solution_> create(ListVariableDescriptor<Solution_> listVariableDescriptor) {
        if (listVariableDescriptor.isValueRangeEntityIndependent()) {
            return new FromSolutionPropertyValueCounter<>(listVariableDescriptor);
        } else {
            return new FromEntityPropertyValueCounter<>(listVariableDescriptor);
        }
    }

    static <Solution_> int countValues(ListVariableDescriptor<Solution_> listVariableDescriptor, Solution_ solution) {
        if (listVariableDescriptor.isValueRangeEntityIndependent()) {
            var counter = new FromSolutionPropertyValueCounter<>(listVariableDescriptor);
            counter.resetWorkingSolution(solution);
            return counter.getCount();
        } else {
            var counter = new FromEntityPropertyValueCounter<>(listVariableDescriptor);
            counter.resetWorkingSolution(solution);
            for (var entity : listVariableDescriptor.getEntityDescriptor().extractEntities(solution)) {
                counter.addEntity(entity);
            }
            return counter.getCount();
        }
    }

    void resetWorkingSolution(Solution_ workingSolution);

    void addEntity(Object entity);

    void removeEntity(Object entity);

    int getCount();

}
