package ai.timefold.solver.core.impl.domain.variable;

import java.util.Map;
import java.util.Objects;

import ai.timefold.solver.core.api.score.director.ScoreDirector;
import ai.timefold.solver.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import ai.timefold.solver.core.impl.heuristic.selector.list.ElementLocation;
import ai.timefold.solver.core.impl.heuristic.selector.list.LocationInList;
import ai.timefold.solver.core.impl.heuristic.selector.value.ValueCounter;
import ai.timefold.solver.core.impl.util.CollectionUtils;

final class ExternalizedListVariableStateSupply<Solution_>
        implements ListVariableStateSupply<Solution_> {

    private final ListVariableDescriptor<Solution_> sourceVariableDescriptor;
    private final ValueCounter<Solution_> valueCounter;
    private Map<Object, LocationInList> elementLocationMap;
    private int assignedCount;

    public ExternalizedListVariableStateSupply(ListVariableDescriptor<Solution_> sourceVariableDescriptor) {
        this.sourceVariableDescriptor = sourceVariableDescriptor;
        this.valueCounter = ValueCounter.create(sourceVariableDescriptor);
    }

    @Override
    public void resetWorkingSolution(ScoreDirector<Solution_> scoreDirector) {
        var workingSolution = scoreDirector.getWorkingSolution();
        valueCounter.resetWorkingSolution(workingSolution);
        if (elementLocationMap == null) {
            // Value count will only be present here if the value range is on the solution.
            // Otherwise the values will be counted per entity and the map will grow as necessary.
            var valueCount = valueCounter.getCount();
            elementLocationMap = CollectionUtils.newIdentityHashMap(valueCount == 0 ? 16 : valueCount);
        } else {
            elementLocationMap.clear();
        }
        assignedCount = 0;
        // Will run over all entities and unmark all present elements as unassigned.
        sourceVariableDescriptor.getEntityDescriptor().visitAllEntities(workingSolution, this::insert);
    }

    private void insert(Object entity) {
        valueCounter.addEntity(entity);
        var assignedElements = sourceVariableDescriptor.getValue(entity);
        var index = 0;
        for (var element : assignedElements) {
            var newLocation = new LocationInList(entity, index);
            var oldLocation = elementLocationMap.put(element, newLocation);
            if (oldLocation != null) {
                throw new IllegalStateException(
                        "The supply (%s) is corrupted, because the element (%s) at index (%d) already exists (%s)."
                                .formatted(this, element, index, oldLocation));
            }
            index++;
            assignedCount++;
        }
    }

    @Override
    public void close() {
        elementLocationMap = null;
    }

    @Override
    public void beforeEntityAdded(ScoreDirector<Solution_> scoreDirector, Object o) {
        // No need to do anything.
    }

    @Override
    public void afterEntityAdded(ScoreDirector<Solution_> scoreDirector, Object o) {
        insert(o);
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector<Solution_> scoreDirector, Object o) {
        // No need to do anything.
    }

    @Override
    public void afterEntityRemoved(ScoreDirector<Solution_> scoreDirector, Object o) {
        // When the entity is removed, its values become unassigned.
        // An unassigned value has no inverse entity and no index.
        retract(o);
    }

    private void retract(Object entity) {
        valueCounter.removeEntity(entity);
        var assignedElements = sourceVariableDescriptor.getValue(entity);
        for (var index = 0; index < assignedElements.size(); index++) {
            var element = assignedElements.get(index);
            var oldElementLocation = elementLocationMap.remove(element);
            if (oldElementLocation == null) {
                throw new IllegalStateException(
                        "The supply (%s) is corrupted, because the element (%s) at index (%d) was already unassigned (%s)."
                                .formatted(this, element, index, oldElementLocation));
            }
            var oldIndex = oldElementLocation.index();
            if (oldIndex != index) {
                throw new IllegalStateException(
                        "The supply (%s) is corrupted, because the element (%s) at index (%d) had an old index (%d) which is not the current index (%d)."
                                .formatted(this, element, index, oldIndex, index));
            }
            assignedCount--;
        }
    }

    @Override
    public void afterListVariableElementUnassigned(ScoreDirector<Solution_> scoreDirector, Object element) {
        var oldLocation = elementLocationMap.remove(element);
        if (oldLocation == null) {
            throw new IllegalStateException(
                    "The supply (%s) is corrupted, because the element (%s) did not exist before unassigning."
                            .formatted(this, element));
        }
        assignedCount--;
    }

    @Override
    public void beforeListVariableChanged(ScoreDirector<Solution_> scoreDirector, Object o, int fromIndex, int toIndex) {
        // No need to do anything.
    }

    @Override
    public void afterListVariableChanged(ScoreDirector<Solution_> scoreDirector, Object o, int fromIndex, int toIndex) {
        updateIndexes(o, fromIndex, toIndex);
    }

    private void updateIndexes(Object entity, int startIndex, int toIndex) {
        var assignedElements = sourceVariableDescriptor.getValue(entity);
        for (var index = startIndex; index < assignedElements.size(); index++) {
            var element = assignedElements.get(index);
            var newLocation = new LocationInList(entity, index);
            var oldLocation = elementLocationMap.put(element, newLocation);
            if (oldLocation == null) {
                assignedCount++;
            } else if (index >= toIndex && newLocation.equals(oldLocation)) {
                // Location is unchanged and we are past the part of the list that changed.
                return;
            } else {
                // Continue to the next element.
            }
        }
    }

    @Override
    public ElementLocation getLocationInList(Object planningValue) {
        return Objects.requireNonNullElse(elementLocationMap.get(Objects.requireNonNull(planningValue)),
                ElementLocation.unassigned());
    }

    @Override
    public Integer getIndex(Object planningValue) {
        var elementLocation = elementLocationMap.get(Objects.requireNonNull(planningValue));
        if (elementLocation == null) {
            return null;
        }
        return elementLocation.index();
    }

    @Override
    public Object getInverseSingleton(Object planningValue) {
        var elementLocation = elementLocationMap.get(Objects.requireNonNull(planningValue));
        if (elementLocation == null) {
            return null;
        }
        return elementLocation.entity();
    }

    @Override
    public boolean isAssigned(Object element) {
        return getLocationInList(element) instanceof LocationInList;
    }

    @Override
    public int getUnassignedCount() {
        return valueCounter.getCount() - assignedCount;
    }

    @Override
    public ListVariableDescriptor<Solution_> getSourceVariableDescriptor() {
        return sourceVariableDescriptor;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + sourceVariableDescriptor.getVariableName() + ")";
    }

}
