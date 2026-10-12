package ai.timefold.solver.core.impl.heuristic.selector.move.generic;

import ai.timefold.solver.core.impl.constructionheuristic.ConstructionHeuristicPhase;
import ai.timefold.solver.core.impl.constructionheuristic.DefaultConstructionHeuristicPhase;
import ai.timefold.solver.core.impl.constructionheuristic.scope.ConstructionHeuristicPhaseScope;
import ai.timefold.solver.core.impl.phase.PhaseType;
import ai.timefold.solver.core.impl.solver.scope.SolverScope;

import org.jspecify.annotations.NullMarked;

@NullMarked
public final class RuinRecreateConstructionHeuristicPhase<Solution_>
        extends DefaultConstructionHeuristicPhase<Solution_>
        implements ConstructionHeuristicPhase<Solution_> {

    RuinRecreateConstructionHeuristicPhase(RuinRecreateConstructionHeuristicPhaseBuilder<Solution_> builder) {
        super(builder);
    }

    @Override
    protected ConstructionHeuristicPhaseScope<Solution_> buildPhaseScope(SolverScope<Solution_> solverScope, int phaseIndex) {
        return new RuinRecreateConstructionHeuristicPhaseScope<>(solverScope, phaseIndex);
    }

    @Override
    protected boolean isNested() {
        return true;
    }

    @Override
    public PhaseType getPhaseType() {
        return PhaseType.RUIN_AND_RECREATE_CONSTRUCTION_HEURISTIC;
    }
}
