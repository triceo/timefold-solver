package ai.timefold.solver.core.api.move;

import ai.timefold.solver.core.api.domain.metamodel.LocationInList;
import ai.timefold.solver.core.api.domain.metamodel.PlanningListVariableMetaModel;
import ai.timefold.solver.core.api.domain.metamodel.PlanningVariableMetaModel;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.variable.PlanningListVariable;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import ai.timefold.solver.core.api.score.director.ScoreDirector;

/**
 * Contains all reading and mutating methods available to a {@link Move}
 * in order to change the state of a {@link PlanningSolution planning solution}.
 * <p>
 * <strong>This package and all of its contents are part of the Move Streams API,
 * which is under development and is only offered as a preview feature.</strong>
 * There are no guarantees for backward compatibility;
 * any class, method or field may change or be removed without prior notice,
 * although we will strive to avoid this as much as possible.
 * <p>
 * We encourage you to try the API and give us feedback on your experience with it,
 * before we finalize the API.
 * Please direct your feedback to
 * <a href="https://github.com/TimefoldAI/timefold-solver/discussions">Timefold Solver Github</a>.
 * 
 * @param <Solution_>
 */
public interface MutableSolutionState<Solution_> extends SolutionState<Solution_> {

    /**
     * Reads the value of a @{@link PlanningVariable basic planning variable} of a given entity.
     * 
     * @param variableMetaModel never null
     * @param entity never null
     * @param newValue maybe null, if unassigning the variable
     * @param <Entity_>
     * @param <Value_>
     */
    <Entity_, Value_> void changeVariable(PlanningVariableMetaModel<Solution_, Entity_, Value_> variableMetaModel,
            Entity_ entity, Value_ newValue);

    <Entity_, Value_> void assignValue(PlanningListVariableMetaModel<Solution_, Entity_, Value_> variableMetaModel,
            Value_ value, Entity_ destinationEntity, int destinationIndex);

    @SuppressWarnings("unchecked")
    default <Entity_, Value_> void unassignValue(PlanningListVariableMetaModel<Solution_, Entity_, Value_> variableMetaModel,
            Value_ value) {
        var locationInList = (LocationInList<Entity_>) getPositionOf(variableMetaModel, value)
                .ensureAssigned(() -> """
                        The value (%s) is not assigned to a list variable.
                        This may indicate score corruption or a problem with the move's implementation."""
                        .formatted(value));
        unassignValue(variableMetaModel, value, locationInList.entity(), locationInList.index());
    }

    default <Entity_, Value_> Value_ unassignValue(PlanningListVariableMetaModel<Solution_, Entity_, Value_> variableMetaModel,
            Entity_ entity, int index) {
        var value = getValueAtIndex(variableMetaModel, entity, index);
        unassignValue(variableMetaModel, value, entity, index);
        return value;
    }

    <Entity_, Value_> void unassignValue(PlanningListVariableMetaModel<Solution_, Entity_, Value_> variableMetaModel,
            Value_ value, Entity_ entity, int index);

    /**
     * TODO should check if pinned?
     * Moves a value from one entity's {@link PlanningListVariable planning list variable} to another.
     * 
     * @param variableMetaModel never null
     * @param sourceEntity never null
     * @param sourceIndex >= 0
     * @param destinationEntity never null
     * @param destinationIndex >= 0
     * @return old value
     * @throws IndexOutOfBoundsException if the index is out of bounds
     * @param <Entity_>
     * @param <Value_>
     */
    <Entity_, Value_> Value_ moveValueBetweenLists(PlanningListVariableMetaModel<Solution_, Entity_, Value_> variableMetaModel,
            Entity_ sourceEntity, int sourceIndex, Entity_ destinationEntity, int destinationIndex);

    /**
     * TODO should check if pinned?
     * Moves a value within one entity's {@link PlanningListVariable planning list variable}.
     *
     * @param variableMetaModel never null
     * @param entity never null
     * @param sourceIndex >= 0
     * @param destinationIndex >= 0
     * @return old value
     * @throws IndexOutOfBoundsException if the index is out of bounds
     * @param <Entity_>
     * @param <Value_>
     */
    <Entity_, Value_> Value_ moveValueInList(PlanningListVariableMetaModel<Solution_, Entity_, Value_> variableMetaModel,
            Entity_ entity,
            int sourceIndex, int destinationIndex);

    /**
     * Tells the underlying {@link ScoreDirector}
     * to notify the solver of the mutating operations performed by the {@link Move}.
     */
    void updateShadowVariables();

}
