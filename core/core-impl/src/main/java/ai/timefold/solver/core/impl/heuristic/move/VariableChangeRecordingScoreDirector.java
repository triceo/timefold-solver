package ai.timefold.solver.core.impl.heuristic.move;

import java.util.ArrayList;
import java.util.List;

import ai.timefold.solver.core.api.score.director.ScoreDirector;
import ai.timefold.solver.core.impl.domain.solution.descriptor.SolutionDescriptor;
import ai.timefold.solver.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import ai.timefold.solver.core.impl.domain.variable.descriptor.VariableDescriptor;
import ai.timefold.solver.core.impl.score.director.AbstractScoreDirector;
import ai.timefold.solver.core.impl.score.director.VariableDescriptorAwareScoreDirector;
import ai.timefold.solver.core.impl.score.director.VariableDescriptorCache;

final class VariableChangeRecordingScoreDirector<Solution_> implements VariableDescriptorAwareScoreDirector<Solution_> {

    private final AbstractScoreDirector<Solution_, ?, ?> delegate;
    private final List<ChangeAction<Solution_>> variableChanges;

    VariableChangeRecordingScoreDirector(ScoreDirector<Solution_> delegate) {
        this.delegate = (AbstractScoreDirector<Solution_, ?, ?>) delegate;
        this.variableChanges = new ArrayList<>();
    }

    public List<ChangeAction<Solution_>> getVariableChanges() {
        return variableChanges;
    }

    // For variable change operations, record the change then call the delegate

    @Override
    public void beforeVariableChanged(VariableDescriptor<Solution_> variableDescriptor, Object entity) {
        variableChanges.add(new VariableChangeAction<>(entity, variableDescriptor.getValue(entity), variableDescriptor));
        delegate.beforeVariableChanged(variableDescriptor, entity);
    }

    @Override
    public void afterVariableChanged(VariableDescriptor<Solution_> variableDescriptor, Object entity) {
        delegate.afterVariableChanged(variableDescriptor, entity);
    }

    @Override
    public void beforeListVariableChanged(ListVariableDescriptor<Solution_> variableDescriptor, Object entity, int fromIndex,
            int toIndex) {
        // List is fromIndex, fromIndex, since the undo action for afterListVariableChange will clear the affected list
        variableChanges.add(new ListVariableBeforeChangeAction<>(entity,
                new ArrayList<>(variableDescriptor.getValue(entity).subList(fromIndex, toIndex)), fromIndex, toIndex,
                variableDescriptor));
        delegate.beforeListVariableChanged(variableDescriptor, entity, fromIndex, toIndex);
    }

    @Override
    public void afterListVariableChanged(ListVariableDescriptor<Solution_> variableDescriptor, Object entity, int fromIndex,
            int toIndex) {
        variableChanges.add(new ListVariableAfterChangeAction<>(entity, fromIndex, toIndex, variableDescriptor));
        delegate.afterListVariableChanged(variableDescriptor, entity, fromIndex, toIndex);
    }

    @Override
    public void beforeListVariableElementAssigned(ListVariableDescriptor<Solution_> variableDescriptor, Object element) {
        variableChanges.add(new ListVariableBeforeAssignmentAction<>(element, variableDescriptor));
        delegate.beforeListVariableElementAssigned(variableDescriptor, element);
    }

    @Override
    public void afterListVariableElementAssigned(ListVariableDescriptor<Solution_> variableDescriptor, Object element) {
        variableChanges.add(new ListVariableAfterAssignmentAction<>(element, variableDescriptor));
        delegate.afterListVariableElementAssigned(variableDescriptor, element);
    }

    @Override
    public void beforeListVariableElementUnassigned(ListVariableDescriptor<Solution_> variableDescriptor, Object element) {
        variableChanges.add(new ListVariableBeforeUnassignmentAction<>(element, variableDescriptor));
        delegate.beforeListVariableElementUnassigned(variableDescriptor, element);
    }

    @Override
    public void afterListVariableElementUnassigned(ListVariableDescriptor<Solution_> variableDescriptor, Object element) {
        variableChanges.add(new ListVariableAfterUnassignmentAction<>(element, variableDescriptor));
        delegate.afterListVariableElementUnassigned(variableDescriptor, element);
    }

    // For other operations, call the delegate's method.

    @Override
    public SolutionDescriptor<Solution_> getSolutionDescriptor() {
        return delegate.getSolutionDescriptor();
    }

    @Override
    public Solution_ getWorkingSolution() {
        return delegate.getWorkingSolution();
    }

    @Override
    public VariableDescriptorCache<Solution_> getVariableDescriptorCache() {
        return delegate.getVariableDescriptorCache();
    }

    @Override
    public void triggerVariableListeners() {
        delegate.triggerVariableListeners();
    }

    @Override
    public <E> E lookUpWorkingObject(E externalObject) {
        return delegate.lookUpWorkingObject(externalObject);
    }

    @Override
    public <E> E lookUpWorkingObjectOrReturnNull(E externalObject) {
        return delegate.lookUpWorkingObjectOrReturnNull(externalObject);
    }

    @Override
    public void changeVariableFacade(VariableDescriptor<Solution_> variableDescriptor, Object entity, Object newValue) {
        delegate.changeVariableFacade(variableDescriptor, entity, newValue);
    }

}
