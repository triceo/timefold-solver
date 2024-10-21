package ai.timefold.solver.core.impl.move.generic;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.metamodel.PlanningListVariableMetaModel;
import ai.timefold.solver.core.api.move.Move;
import ai.timefold.solver.core.api.move.MutableSolutionState;
import ai.timefold.solver.core.api.move.SolutionState;

public final class ListUnassignMove<Solution_, Entity_, Value_> extends AbstractMove<Solution_> {

    private final PlanningListVariableMetaModel<Solution_, Entity_, Value_> variableMetaModel;
    private final Value_ movedValue;
    private final Entity_ sourceEntity;
    private final int sourceIndex;

    public ListUnassignMove(PlanningListVariableMetaModel<Solution_, Entity_, Value_> variableMetaModel, Value_ value,
            Entity_ sourceEntity, int sourceIndex) {
        this.variableMetaModel = variableMetaModel;
        this.movedValue = value;
        this.sourceEntity = sourceEntity;
        this.sourceIndex = sourceIndex;
    }

    @Override
    public boolean isMoveDoable(SolutionState<Solution_> solutionState) {
        return solutionState.getValueCount(variableMetaModel, sourceEntity) > sourceIndex;
    }

    @Override
    public void run(MutableSolutionState<Solution_> mutableSolutionState) {
        mutableSolutionState.unassignValue(variableMetaModel, movedValue, sourceEntity, sourceIndex);
    }

    @Override
    public Move<Solution_> rebase(SolutionState<Solution_> solutionState) {
        return new ListUnassignMove<>(variableMetaModel, solutionState.rebase(movedValue), solutionState.rebase(sourceEntity),
                sourceIndex);
    }

    @Override
    public Collection<?> getPlanningEntities() {
        return Collections.singleton(sourceEntity);
    }

    @Override
    public Collection<?> getPlanningValues() {
        return Collections.singleton(movedValue);
    }

    @Override
    public String getMoveTypeDescription() {
        return getClass().getSimpleName() + "(" + getVariableDescriptor(variableMetaModel).getSimpleEntityAndVariableName()
                + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ListUnassignMove<?, ?, ?> that))
            return false;
        return sourceIndex == that.sourceIndex && Objects.equals(variableMetaModel, that.variableMetaModel)
                && Objects.equals(sourceEntity, that.sourceEntity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variableMetaModel, sourceEntity, sourceIndex);
    }

    @Override
    public String toString() {
        return String.format("%s {%s[%d] -> null}", movedValue, sourceEntity, sourceIndex);
    }
}
