package ai.timefold.solver.core.impl.heuristic.selector.move.generic.list.ruin;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.SequencedSet;
import java.util.TreeSet;

import ai.timefold.solver.core.api.domain.common.Lookup;
import ai.timefold.solver.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import ai.timefold.solver.core.impl.heuristic.move.AbstractSelectorBasedMove;
import ai.timefold.solver.core.impl.heuristic.selector.move.generic.RuinRecreateConstructionHeuristicPhase;
import ai.timefold.solver.core.impl.heuristic.selector.move.generic.RuinRecreateConstructionHeuristicPhaseBuilder;
import ai.timefold.solver.core.impl.move.MoveDirector;
import ai.timefold.solver.core.impl.score.director.InnerScoreDirector;
import ai.timefold.solver.core.impl.score.director.VariableDescriptorAwareScoreDirector;
import ai.timefold.solver.core.impl.solver.random.DefaultRandomSource;
import ai.timefold.solver.core.impl.solver.scope.SolverScope;
import ai.timefold.solver.core.preview.api.domain.metamodel.PositionInList;
import ai.timefold.solver.core.preview.api.move.Move;

import org.jspecify.annotations.NullMarked;

@NullMarked
public final class SelectorBasedListRuinRecreateMove<Solution_> extends AbstractSelectorBasedMove<Solution_> {

    private final ListVariableDescriptor<Solution_> listVariableDescriptor;
    private final List<Object> ruinedValueList;
    private final SequencedSet<Object> affectedEntitySet;
    private final RuinRecreateConstructionHeuristicPhaseBuilder<Solution_> constructionHeuristicPhaseBuilder;
    private final SolverScope<Solution_> solverScope;
    private final Map<Object, NavigableSet<RuinedPosition>> entityToNewPositionMap;
    private final long randomSeed;

    /**
     * Prefer
     * {@link #SelectorBasedListRuinRecreateMove(ListVariableDescriptor, RuinRecreateConstructionHeuristicPhaseBuilder, SolverScope, List, SequencedSet, long)}
     * with explicitly provided random seed.
     *
     * @param listVariableDescriptor
     * @param constructionHeuristicPhaseBuilder
     * @param solverScope
     * @param ruinedValueList
     * @param affectedEntitySet
     */
    public SelectorBasedListRuinRecreateMove(ListVariableDescriptor<Solution_> listVariableDescriptor,
            RuinRecreateConstructionHeuristicPhaseBuilder<Solution_> constructionHeuristicPhaseBuilder,
            SolverScope<Solution_> solverScope, List<Object> ruinedValueList, SequencedSet<Object> affectedEntitySet) {
        this(listVariableDescriptor, constructionHeuristicPhaseBuilder, solverScope, ruinedValueList, affectedEntitySet, 0);
    }

    public SelectorBasedListRuinRecreateMove(ListVariableDescriptor<Solution_> listVariableDescriptor,
            RuinRecreateConstructionHeuristicPhaseBuilder<Solution_> constructionHeuristicPhaseBuilder,
            SolverScope<Solution_> solverScope, List<Object> ruinedValueList, SequencedSet<Object> affectedEntitySet,
            long randomSeed) {
        this.listVariableDescriptor = listVariableDescriptor;
        this.constructionHeuristicPhaseBuilder = constructionHeuristicPhaseBuilder;
        this.solverScope = solverScope;
        this.ruinedValueList = ruinedValueList;
        this.affectedEntitySet = affectedEntitySet;
        this.entityToNewPositionMap = new IdentityHashMap<>(affectedEntitySet.size());
        this.randomSeed = randomSeed;
    }

    @Override
    protected void execute(VariableDescriptorAwareScoreDirector<Solution_> scoreDirector) {
        entityToNewPositionMap.clear();
        var innerScoreDirector = (InnerScoreDirector<Solution_, ?>) scoreDirector;
        var listVariableState = innerScoreDirector.getListVariableState(listVariableDescriptor);
        var entityToOriginalPositionMap =
                new IdentityHashMap<Object, NavigableSet<RuinedPosition>>(affectedEntitySet.size());
        for (var valueToRuin : ruinedValueList) {
            var position = listVariableState.getElementPosition(valueToRuin)
                    .ensureAssigned();
            entityToOriginalPositionMap.computeIfAbsent(position.entity(),
                    ignored -> new TreeSet<>()).add(new RuinedPosition(valueToRuin, position.index()));
        }

        for (var entry : entityToOriginalPositionMap.entrySet()) {
            var entity = entry.getKey();
            var originalPositionSet = entry.getValue();

            scoreDirector.beforeListVariableChanged(listVariableDescriptor, entity,
                    listVariableDescriptor.getFirstUnpinnedIndex(entity),
                    listVariableDescriptor.getListSize(entity));
            for (var position : originalPositionSet.descendingSet()) {
                scoreDirector.beforeListVariableElementUnassigned(listVariableDescriptor, position.ruinedValue());
                listVariableDescriptor.removeElement(entity, position.index());
                scoreDirector.afterListVariableElementUnassigned(listVariableDescriptor, position.ruinedValue());
            }
            scoreDirector.afterListVariableChanged(listVariableDescriptor, entity,
                    listVariableDescriptor.getFirstUnpinnedIndex(entity),
                    listVariableDescriptor.getListSize(entity));
        }
        scoreDirector.updateShadowVariables();

        var constructionHeuristicPhase =
                (RuinRecreateConstructionHeuristicPhase<Solution_>) constructionHeuristicPhaseBuilder
                        .ensureThreadSafe(innerScoreDirector)
                        .withElementsToRecreate(ruinedValueList)
                        .build();

        var nestedSolverScope = new SolverScope<Solution_>(solverScope.getClock());
        nestedSolverScope.setSolver(solverScope.getSolver());
        nestedSolverScope.setScoreDirector(innerScoreDirector);
        nestedSolverScope.setWorkingRandom(DefaultRandomSource.seeded(randomSeed));
        constructionHeuristicPhase.solvingStarted(nestedSolverScope);
        constructionHeuristicPhase.solve(nestedSolverScope);
        constructionHeuristicPhase.solvingEnded(nestedSolverScope);
        scoreDirector.updateShadowVariables();

        for (var ruinedValue : ruinedValueList) {
            // Some ruined values may be left unassigned
            if (listVariableState.getElementPosition(ruinedValue) instanceof PositionInList position) {
                entityToNewPositionMap.computeIfAbsent(position.entity(), ignored -> new TreeSet<>())
                        .add(new RuinedPosition(ruinedValue, position.index()));
            }
        }
    }

    @Override
    public SequencedCollection<Object> getPlanningEntities() {
        return affectedEntitySet;
    }

    @Override
    public SequencedCollection<Object> getPlanningValues() {
        return ruinedValueList;
    }

    @Override
    public Move<Solution_> rebase(Lookup lookup) {
        var rebasedRuinedValueList = AbstractSelectorBasedMove.rebaseList(ruinedValueList, lookup);
        var rebasedAffectedEntitySet = AbstractSelectorBasedMove.rebaseSet(affectedEntitySet, lookup);
        var rebasedListVariableDescriptor = ((MoveDirector<Solution_, ?>) lookup)
                .getScoreDirector()
                .getSolutionDescriptor()
                .getListVariableDescriptor();
        return new SelectorBasedListRuinRecreateMove<>(rebasedListVariableDescriptor, constructionHeuristicPhaseBuilder,
                solverScope, rebasedRuinedValueList, rebasedAffectedEntitySet, randomSeed);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SelectorBasedListRuinRecreateMove<?> that))
            return false;
        return Objects.equals(listVariableDescriptor, that.listVariableDescriptor)
                && Objects.equals(ruinedValueList, that.ruinedValueList)
                && Objects.equals(affectedEntitySet, that.affectedEntitySet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(listVariableDescriptor, ruinedValueList, affectedEntitySet);
    }

    @Override
    public String toString() {
        return "ListRuinMove{" +
                "values=" + ruinedValueList +
                ", newPositionsByEntity=" + (!entityToNewPositionMap.isEmpty() ? entityToNewPositionMap : "?") +
                '}';
    }
}
