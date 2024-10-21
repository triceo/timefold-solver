package ai.timefold.solver.core.impl.move.generic;

import java.util.Objects;

import ai.timefold.solver.core.api.domain.metamodel.PlanningVariableMetaModel;
import ai.timefold.solver.core.api.move.MutableSolutionState;
import ai.timefold.solver.core.api.move.SolutionState;
import ai.timefold.solver.core.impl.domain.variable.inverserelation.SingletonInverseVariableSupply;

public final class ChainedChangeMove<Solution_, Entity_> extends ChangeMove<Solution_, Entity_, Entity_> {

    private final Entity_ oldTrailingEntity;
    private final Entity_ newTrailingEntity;

    @SuppressWarnings("unchecked")
    public ChainedChangeMove(PlanningVariableMetaModel<Solution_, Entity_, Entity_> variableMetaModel, Entity_ entity,
            Entity_ toPlanningValue, SingletonInverseVariableSupply inverseVariableSupply) {
        super(variableMetaModel, entity, toPlanningValue);
        oldTrailingEntity = (Entity_) inverseVariableSupply.getInverseSingleton(entity);
        newTrailingEntity = toPlanningValue == null ? null
                : (Entity_) inverseVariableSupply.getInverseSingleton(toPlanningValue);
    }

    public ChainedChangeMove(PlanningVariableMetaModel<Solution_, Entity_, Entity_> variableMetaModel, Entity_ entity,
            Entity_ toPlanningValue, Entity_ oldTrailingEntity, Entity_ newTrailingEntity) {
        super(variableMetaModel, entity, toPlanningValue);
        this.oldTrailingEntity = oldTrailingEntity;
        this.newTrailingEntity = newTrailingEntity;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public boolean isMoveDoable(SolutionState<Solution_> solutionState) {
        return super.isMoveDoable(solutionState)
                && !Objects.equals(entity, toPlanningValue);
    }

    @Override
    public void run(MutableSolutionState<Solution_> solutionState) {
        // Close the old chain
        if (oldTrailingEntity != null) {
            solutionState.changeVariable(variableMetaModel, oldTrailingEntity, readValue(solutionState));
        }
        // Change the entity
        solutionState.changeVariable(variableMetaModel, entity, toPlanningValue);
        // Reroute the new chain
        if (newTrailingEntity != null) {
            solutionState.changeVariable(variableMetaModel, newTrailingEntity, entity);
        }
    }

    @Override
    public ChainedChangeMove<Solution_, Entity_> rebase(SolutionState<Solution_> solutionState) {
        return new ChainedChangeMove<>(variableMetaModel,
                solutionState.rebase(entity),
                solutionState.rebase(toPlanningValue),
                solutionState.rebase(oldTrailingEntity),
                solutionState.rebase(newTrailingEntity));
    }

}
