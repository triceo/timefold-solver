package ai.timefold.solver.core.impl.move.director;

import java.util.Objects;

import ai.timefold.solver.core.api.domain.metamodel.ElementLocation;
import ai.timefold.solver.core.api.domain.metamodel.PlanningListVariableMetaModel;
import ai.timefold.solver.core.api.domain.metamodel.PlanningVariableMetaModel;
import ai.timefold.solver.core.impl.domain.solution.descriptor.DefaultPlanningListVariableMetaModel;
import ai.timefold.solver.core.impl.domain.solution.descriptor.DefaultPlanningVariableMetaModel;
import ai.timefold.solver.core.impl.domain.variable.descriptor.BasicVariableDescriptor;
import ai.timefold.solver.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import ai.timefold.solver.core.impl.move.InnerMutableSolutionState;
import ai.timefold.solver.core.impl.score.director.InnerScoreDirector;
import ai.timefold.solver.core.impl.score.director.VariableDescriptorAwareScoreDirector;

public sealed class MoveDirector<Solution_> implements InnerMutableSolutionState<Solution_>
        permits EphemeralMoveDirector {

    protected final VariableDescriptorAwareScoreDirector<Solution_> scoreDirector;

    public MoveDirector(VariableDescriptorAwareScoreDirector<Solution_> scoreDirector) {
        this.scoreDirector = Objects.requireNonNull(scoreDirector);
    }

    public final <Entity_, Value_> void changeVariable(PlanningVariableMetaModel<Solution_, Entity_, Value_> variableMetaModel,
            Entity_ entity, Value_ newValue) {
        var variableDescriptor = extractVariableDescriptor(variableMetaModel);
        scoreDirector.beforeVariableChanged(variableDescriptor, entity);
        variableDescriptor.setValue(entity, newValue);
        scoreDirector.afterVariableChanged(variableDescriptor, entity);
    }

    @Override
    public <Entity_, Value_> void assignValue(PlanningListVariableMetaModel<Solution_, Entity_, Value_> variableMetaModel,
            Value_ value, Entity_ destinationEntity, int destinationIndex) {
        var variableDescriptor = extractVariableDescriptor(variableMetaModel);
        scoreDirector.beforeListVariableElementAssigned(variableDescriptor, value);
        scoreDirector.beforeListVariableChanged(variableDescriptor, destinationEntity, destinationIndex, destinationIndex);
        variableDescriptor.addElement(destinationEntity, destinationIndex, value);
        scoreDirector.afterListVariableChanged(variableDescriptor, destinationEntity, destinationIndex, destinationIndex + 1);
        scoreDirector.afterListVariableElementAssigned(variableDescriptor, value);
    }

    @Override
    public <Entity_, Value_> void unassignValue(PlanningListVariableMetaModel<Solution_, Entity_, Value_> variableMetaModel,
            Value_ value, Entity_ entity, int index) {
        var variableDescriptor = extractVariableDescriptor(variableMetaModel);
        scoreDirector.beforeListVariableElementUnassigned(variableDescriptor, value);
        scoreDirector.beforeListVariableChanged(variableDescriptor, entity, index, index);
        var oldValue = variableDescriptor.removeElement(entity, index);
        if (oldValue != value) {
            throw new IllegalStateException("""
                    The value (%s) removed from the entity (%s) at index (%d) was not the expected value (%s).
                    This may indicate a score corruption or a problem with the move's implementation."""
                    .formatted(oldValue, entity, index, value));
        }
        scoreDirector.afterListVariableChanged(variableDescriptor, entity, index, index);
        scoreDirector.afterListVariableElementUnassigned(variableDescriptor, value);
    }

    public final <Entity_, Value_> Value_ moveValueBetweenLists(
            PlanningListVariableMetaModel<Solution_, Entity_, Value_> variableMetaModel,
            Entity_ sourceEntity, int sourceIndex, Entity_ destinationEntity, int destinationIndex) {
        if (sourceEntity == destinationEntity) {
            return moveValueInList(variableMetaModel, sourceEntity, sourceIndex, destinationIndex);
        }
        var variableDescriptor = extractVariableDescriptor(variableMetaModel);
        scoreDirector.beforeListVariableChanged(variableDescriptor, sourceEntity, sourceIndex, sourceIndex + 1);
        Value_ element = variableDescriptor.removeElement(sourceEntity, sourceIndex);
        scoreDirector.afterListVariableChanged(variableDescriptor, sourceEntity, sourceIndex, sourceIndex);

        scoreDirector.beforeListVariableChanged(variableDescriptor, destinationEntity, destinationIndex, destinationIndex);
        variableDescriptor.addElement(destinationEntity, destinationIndex, element);
        scoreDirector.afterListVariableChanged(variableDescriptor, destinationEntity, destinationIndex, destinationIndex + 1);
        return element;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <Entity_, Value_> Value_ moveValueInList(
            PlanningListVariableMetaModel<Solution_, Entity_, Value_> variableMetaModel, Entity_ entity, int sourceIndex,
            int destinationIndex) {
        if (sourceIndex > destinationIndex) { // Always start from the lower index.
            return moveValueInList(variableMetaModel, entity, destinationIndex, sourceIndex);
        }
        var variableDescriptor = extractVariableDescriptor(variableMetaModel);
        var toIndex = destinationIndex + 1;
        scoreDirector.beforeListVariableChanged(variableDescriptor, entity, sourceIndex, toIndex);
        var variable = variableDescriptor.getValue(entity);
        var value = (Value_) variable.remove(sourceIndex);
        variable.add(destinationIndex, value);
        scoreDirector.afterListVariableChanged(variableDescriptor, entity, sourceIndex, toIndex);
        return value;
    }

    @Override
    public final void updateShadowVariables() {
        scoreDirector.triggerVariableListeners();
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <Entity_, Value_> Value_ getValue(PlanningVariableMetaModel<Solution_, Entity_, Value_> variableMetaModel,
            Entity_ entity) {
        return (Value_) extractVariableDescriptor(variableMetaModel).getValue(entity);
    }

    @Override
    public <Entity_> int getValueCount(PlanningListVariableMetaModel<Solution_, Entity_, ?> variableMetaModel, Entity_ entity) {
        return extractVariableDescriptor(variableMetaModel).getListSize(entity);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <Entity_, Value_> Value_ getValueAtIndex(
            PlanningListVariableMetaModel<Solution_, Entity_, Value_> variableMetaModel,
            Entity_ entity, int index) {
        return (Value_) extractVariableDescriptor(variableMetaModel).getValue(entity).get(index);
    }

    @Override
    public <Entity_, Value_> ElementLocation getPositionOf(
            PlanningListVariableMetaModel<Solution_, Entity_, Value_> variableMetaModel,
            Value_ value) {
        return getPositionOf((InnerScoreDirector<Solution_, ?>) scoreDirector, variableMetaModel, value);
    }

    protected static <Solution_, Entity_, Value_> ElementLocation getPositionOf(InnerScoreDirector<Solution_, ?> scoreDirector,
            PlanningListVariableMetaModel<Solution_, Entity_, Value_> variableMetaModel, Value_ value) {
        return scoreDirector.getListVariableStateSupply(extractVariableDescriptor(variableMetaModel))
                .getLocationInList(value);
    }

    @Override
    public final <T> T rebase(T problemFactOrPlanningEntity) {
        return scoreDirector.lookUpWorkingObject(problemFactOrPlanningEntity);
    }

    private static <Solution_, Entity_, Value_> BasicVariableDescriptor<Solution_>
            extractVariableDescriptor(PlanningVariableMetaModel<Solution_, Entity_, Value_> variableMetaModel) {
        return ((DefaultPlanningVariableMetaModel<Solution_, Entity_, Value_>) variableMetaModel).variableDescriptor();
    }

    private static <Solution_, Entity_, Value_> ListVariableDescriptor<Solution_>
            extractVariableDescriptor(PlanningListVariableMetaModel<Solution_, Entity_, Value_> variableMetaModel) {
        return ((DefaultPlanningListVariableMetaModel<Solution_, Entity_, Value_>) variableMetaModel).variableDescriptor();
    }

    /**
     * Moves that are to be undone later need to be run with the instance returned by this method.
     * 
     * @return never null
     */
    public EphemeralMoveDirector<Solution_> ephemeral() {
        return new EphemeralMoveDirector<>(scoreDirector);
    }

    @Override
    public VariableDescriptorAwareScoreDirector<Solution_> getScoreDirector() {
        return scoreDirector;
    }

}
