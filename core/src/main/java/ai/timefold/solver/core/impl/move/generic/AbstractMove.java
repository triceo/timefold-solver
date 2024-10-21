package ai.timefold.solver.core.impl.move.generic;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ai.timefold.solver.core.api.domain.metamodel.PlanningListVariableMetaModel;
import ai.timefold.solver.core.api.domain.metamodel.PlanningVariableMetaModel;
import ai.timefold.solver.core.api.move.Move;
import ai.timefold.solver.core.api.score.director.ScoreDirector;
import ai.timefold.solver.core.impl.domain.solution.descriptor.DefaultPlanningListVariableMetaModel;
import ai.timefold.solver.core.impl.domain.solution.descriptor.DefaultPlanningVariableMetaModel;
import ai.timefold.solver.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import ai.timefold.solver.core.impl.domain.variable.descriptor.ListVariableDescriptor;

public abstract class AbstractMove<Solution_> implements Move<Solution_> {

    protected GenuineVariableDescriptor<Solution_> getVariableDescriptor(PlanningVariableMetaModel<Solution_, ?, ?> variableMetaModel) {
        return ((DefaultPlanningVariableMetaModel<Solution_, ?, ?>)variableMetaModel).variableDescriptor();
    }

    protected ListVariableDescriptor<Solution_> getVariableDescriptor(PlanningListVariableMetaModel<Solution_, ?, ?> variableMetaModel) {
        return ((DefaultPlanningListVariableMetaModel<Solution_, ?, ?>)variableMetaModel).variableDescriptor();
    }

    public static <E> List<E> rebaseList(List<E> externalObjectList, ScoreDirector<?> destinationScoreDirector) {
        var rebasedObjectList = new ArrayList<E>(externalObjectList.size());
        for (var entity : externalObjectList) {
            rebasedObjectList.add(destinationScoreDirector.lookUpWorkingObject(entity));
        }
        return rebasedObjectList;
    }

    public static <E> Set<E> rebaseSet(Set<E> externalObjectSet, ScoreDirector<?> destinationScoreDirector) {
        var rebasedObjectSet = new LinkedHashSet<E>(externalObjectSet.size());
        for (var entity : externalObjectSet) {
            rebasedObjectSet.add(destinationScoreDirector.lookUpWorkingObject(entity));
        }
        return rebasedObjectSet;
    }

}
