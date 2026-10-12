package ai.timefold.solver.core.impl.move;

import ai.timefold.solver.core.api.domain.common.Lookup;
import ai.timefold.solver.core.impl.score.director.InnerScoreDirector;
import ai.timefold.solver.core.preview.api.move.Move;
import ai.timefold.solver.core.preview.api.move.MutableSolutionView;

import org.jspecify.annotations.NullMarked;

/**
 * Reverts the changes of a temporary move, from a detached copy of its {@link UndoJournal}.
 * It can execute more than once, each time on the state the original move produced.
 */
@NullMarked
record JournalUndoMove<Solution_>(UndoJournal<Solution_> journal) implements Move<Solution_> {

    @SuppressWarnings("unchecked")
    @Override
    public void execute(MutableSolutionView<Solution_> solutionView) {
        var scoreDirector = (InnerScoreDirector<Solution_, ?>) ((InnerMutableSolutionView<Solution_>) solutionView)
                .getScoreDirector();
        // The detached journal does not hold the pending state, so it cannot write it back.
        if (scoreDirector.isShadowVariableUpdatePending()) {
            throw new IllegalStateException("""
                    Impossible state: the undo move (%s) cannot execute while a shadow variable update is pending.
                    Maybe update the shadow variables before executing the undo move.""".formatted(this));
        }
        journal.restoreAndKeep(scoreDirector);
    }

    @Override
    public Move<Solution_> rebase(Lookup lookup) {
        throw new UnsupportedOperationException("An undo move cannot be rebased.");
    }

    @Override
    public String toString() {
        return "Undo(%s)".formatted(journal);
    }

}
