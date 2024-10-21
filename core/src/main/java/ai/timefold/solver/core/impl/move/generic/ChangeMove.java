package ai.timefold.solver.core.impl.move.generic;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.metamodel.PlanningVariableMetaModel;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.move.Move;
import ai.timefold.solver.core.api.move.MutableSolutionState;
import ai.timefold.solver.core.api.move.SolutionState;

/**
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public class ChangeMove<Solution_, Entity_, Value_> extends AbstractMove<Solution_> {

    protected final PlanningVariableMetaModel<Solution_, Entity_, Value_> variableMetaModel;
    protected final Entity_ entity;
    protected final Value_ toPlanningValue;

    private Value_ currentValue;

    public ChangeMove(PlanningVariableMetaModel<Solution_, Entity_, Value_> variableMetaModel, Entity_ entity,
            Value_ toPlanningValue) {
        this.variableMetaModel = variableMetaModel;
        this.entity = entity;
        this.toPlanningValue = toPlanningValue;
    }

    @Override
    public boolean isMoveDoable(SolutionState<Solution_> solutionState) {
        return !Objects.equals(readValue(solutionState), toPlanningValue);
    }

    protected Value_ readValue(SolutionState<Solution_> solutionState) {
        if (currentValue == null) {
            currentValue = solutionState.getValue(variableMetaModel, entity);
        }
        return currentValue;
    }

    @Override
    public void run(MutableSolutionState<Solution_> mutableSolutionState) {
        mutableSolutionState.changeVariable(variableMetaModel, entity, toPlanningValue);
    }

    @Override
    public Move<Solution_> rebase(SolutionState<Solution_> solutionState) {
        return new ChangeMove<>(variableMetaModel, solutionState.rebase(entity), solutionState.rebase(toPlanningValue));
    }

    @Override
    public Collection<?> getPlanningEntities() {
        return Collections.singletonList(entity);
    }

    @Override
    public Collection<?> getPlanningValues() {
        return Collections.singletonList(toPlanningValue);
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
        if (!(o instanceof ChangeMove<?, ?, ?> that))
            return false;
        return Objects.equals(variableMetaModel, that.variableMetaModel) && Objects.equals(entity, that.entity)
                && Objects.equals(toPlanningValue, that.toPlanningValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variableMetaModel, entity, toPlanningValue);
    }

    @Override
    public String toString() {
        var oldValue = currentValue == null ? getVariableDescriptor(variableMetaModel).getValue(entity) : currentValue;
        return entity + " {" + oldValue + " -> " + toPlanningValue + "}";
    }

}
