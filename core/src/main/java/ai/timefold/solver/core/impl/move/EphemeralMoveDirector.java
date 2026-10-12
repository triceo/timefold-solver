package ai.timefold.solver.core.impl.move;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.impl.score.director.InnerScoreDirector;
import ai.timefold.solver.core.preview.api.move.Move;

import org.jspecify.annotations.NullMarked;

/**
 * The only move director that supports undoing moves.
 * It opens an {@link UndoJournal} on the score director when created,
 * and the moves are undone when the director is {@link #close() closed}.
 * This class deliberately is not {@link AutoCloseable}:
 * a try-with-resources statement would undo even a move that threw an exception,
 * whose recorded changes may be half-applied,
 * causing all sorts of unexpected situations.
 * Callers therefore close explicitly, on the paths where the move itself succeeded;
 * a move that throws terminates the execution instead,
 * never getting to the point of triggering its undo.
 *
 * @param <Solution_>
 */
@NullMarked
final class EphemeralMoveDirector<Solution_, Score_ extends Score<Score_>>
        extends MoveDirector<Solution_, Score_> {

    private final InnerScoreDirector<Solution_, Score_> scoreDirector;
    private final UndoJournal<Solution_> journal;

    EphemeralMoveDirector(InnerScoreDirector<Solution_, Score_> scoreDirector) {
        super(scoreDirector);
        this.scoreDirector = scoreDirector;
        this.journal = scoreDirector.openUndoJournal();
    }

    /**
     * @return a move that reverts the changes recorded so far, when executed on the state they produced
     */
    Move<Solution_> createUndoMove() {
        return new JournalUndoMove<>(journal.copy());
    }

    public void close() {
        scoreDirector.closeUndoJournal(journal);
        journal.restore(scoreDirector);
    }

}
