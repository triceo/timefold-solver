package ai.timefold.solver.core.impl.move.generic;

import java.util.Collection;
import java.util.List;

import ai.timefold.solver.core.api.domain.metamodel.ListVariableMetaModel;
import ai.timefold.solver.core.api.domain.metamodel.LocationInList;
import ai.timefold.solver.core.api.move.Move;
import ai.timefold.solver.core.api.move.MutableSolutionState;
import ai.timefold.solver.core.api.move.SolutionState;

public record ListChangeMove<Solution_, Entity_, Value_>(ListVariableMetaModel<Solution_, Entity_, Value_> variableMetaModel,
        Value_ value, Value_ insertAfter)
        implements
            Move<Solution_, ListChangeMove.ListChangeMoveContext<Entity_>> {

    @Override
    public ListChangeMoveContext<Entity_> prepareContext(SolutionState<Solution_> solutionState) {
        var sourcePosition = solutionState.getPositionOf(variableMetaModel, value)
                .<Entity_> ensureAssigned();
        var destinationPosition = solutionState.getPositionOf(variableMetaModel, insertAfter)
                .<Entity_> ensureAssigned();
        return new ListChangeMoveContext<>(sourcePosition, destinationPosition);
    }

    @Override
    public void run(MutableSolutionState<Solution_> mutableSolutionState, ListChangeMoveContext<Entity_> ctx) {
        var sourceEntity = ctx.source().entity();
        var sourceIndex = ctx.source().index();
        var destinationEntity = ctx.destination().entity();
        var destinationIndex = ctx.destination().index();
        if (sourceEntity == destinationEntity) {
            mutableSolutionState.moveValueInList(variableMetaModel, sourceEntity, sourceIndex, destinationIndex);
        } else {
            mutableSolutionState.moveValueBetweenLists(variableMetaModel, sourceEntity, sourceIndex, destinationEntity,
                    destinationIndex);
        }
    }

    @Override
    public Move<Solution_, ListChangeMoveContext<Entity_>> rebase(SolutionState<Solution_> solutionState,
            ListChangeMoveContext<Entity_> ctx) {
        return new ListChangeMove<>(variableMetaModel, solutionState.rebase(value), solutionState.rebase(insertAfter));
    }

    @Override
    public Collection<?> getPlanningEntities(ListChangeMoveContext<Entity_> ctx) {
        return List.of(ctx.source.entity(), ctx.destination.entity());
    }

    @Override
    public Collection<?> getPlanningValues(ListChangeMoveContext<Entity_> ctx) {
        return List.of(value);
    }

    @Override
    public String toString(ListChangeMoveContext<Entity_> ctx) {
        return ""; // TODO
    }

    public record ListChangeMoveContext<Entity_>(LocationInList<Entity_> source, LocationInList<Entity_> destination) {
    }

}
