package ai.timefold.solver.core.impl.move.generic;

import java.util.Collection;
import java.util.List;

import ai.timefold.solver.core.api.domain.metamodel.PlanningListVariableMetaModel;
import ai.timefold.solver.core.api.move.Move;
import ai.timefold.solver.core.api.move.MutableSolutionState;
import ai.timefold.solver.core.api.move.SolutionState;

public final class ListAssignMove<Solution_, Entity_, Value_> extends AbstractMove<Solution_> {

    private final PlanningListVariableMetaModel<Solution_, Entity_, Value_> variableMetaModel;
    private final Value_ planningValue;
    private final Entity_ destinationEntity;
    private final int destinationIndex;

    public ListAssignMove(PlanningListVariableMetaModel<Solution_, Entity_, Value_> variableMetaModel, Value_ planningValue,
            Entity_ destinationEntity, int destinationIndex) {
        this.variableMetaModel = variableMetaModel;
        this.planningValue = planningValue;
        this.destinationEntity = destinationEntity;
        this.destinationIndex = destinationIndex;
    }

    @Override
    public boolean isMoveDoable(SolutionState<Solution_> solutionState) {
        return destinationIndex >= 0 && solutionState.getValueCount(variableMetaModel, destinationEntity) >= destinationIndex;
    }

    @Override
    public void run(MutableSolutionState<Solution_> mutableSolutionState) {
        mutableSolutionState.assignValue(variableMetaModel, planningValue, destinationEntity, destinationIndex);
    }

    @Override
    public Move<Solution_> rebase(SolutionState<Solution_> solutionState) {
        return new ListAssignMove<>(variableMetaModel, solutionState.rebase(planningValue),
                solutionState.rebase(destinationEntity), destinationIndex);
    }

    @Override
    public Collection<?> getPlanningEntities() {
        return List.of(destinationEntity);
    }

    @Override
    public Collection<?> getPlanningValues() {
        return List.of(planningValue);
    }

    @Override
    public String getMoveTypeDescription() {
        return getClass().getSimpleName() + "(" + getVariableDescriptor(variableMetaModel).getSimpleEntityAndVariableName() + ")";
    }

    @Override
    public String toString() {
        return String.format("%s {null -> %s[%d]}", planningValue, destinationEntity, destinationIndex);
    }
}
