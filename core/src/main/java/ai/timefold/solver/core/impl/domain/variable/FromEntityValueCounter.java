package ai.timefold.solver.core.impl.domain.variable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.valuerange.CountableValueRange;
import ai.timefold.solver.core.impl.domain.variable.descriptor.ListVariableDescriptor;

final class FromEntityValueCounter<Solution_>
        implements ValueCounter<Solution_> {

    private final ListVariableDescriptor<Solution_> sourceVariableDescriptor;
    private final Map<Object, Integer> valueCountMap = new IdentityHashMap<>();
    private Solution_ workingSolution;

    public FromEntityValueCounter(ListVariableDescriptor<Solution_> sourceVariableDescriptor) {
        this.sourceVariableDescriptor = sourceVariableDescriptor;
    }

    @Override
    public void resetWorkingSolution(Solution_ workingSolution) {
        this.workingSolution = workingSolution;
        valueCountMap.clear();
    }

    @Override
    public void addEntity(Object entity) {
        var valueRange = (CountableValueRange<Object>) sourceVariableDescriptor.getValueRange(workingSolution, entity);
        var iterator = valueRange.createOriginalIterator();
        while (iterator.hasNext()) {
            var value = iterator.next();
            valueCountMap.compute(value, (k, v) -> v == null ? 1 : v + 1);
        }
    }

    @Override
    public void removeEntity(Object entity) {
        var valueRange = (CountableValueRange<Object>) sourceVariableDescriptor.getValueRange(workingSolution, entity);
        var iterator = valueRange.createOriginalIterator();
        while (iterator.hasNext()) {
            var value = iterator.next();
            valueCountMap.compute(value, (k, v) -> Objects.equals(v, 1) ? null : v - 1);
        }
    }

    @Override
    public int getCount() {
        return valueCountMap.size();
    }

}
