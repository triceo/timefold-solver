package ai.timefold.solver.core.impl.solver.event;

import java.util.Objects;
import java.util.function.UnaryOperator;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.event.BestSolutionChangedEvent;
import ai.timefold.solver.core.impl.score.director.InnerScore;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class DefaultBestSolutionChangedEvent<Solution_> extends BestSolutionChangedEvent<Solution_> {

    private final UnaryOperator<Solution_> solutionCloner;
    private final int unassignedCount;

    private volatile @Nullable Solution_ clonedSolution = null;

    public DefaultBestSolutionChangedEvent(@NonNull Solver<Solution_> solver, long timeMillisSpent,
            @NonNull Solution_ newBestSolution, @NonNull InnerScore<?> newBestScore, UnaryOperator<Solution_> solutionCloner) {
        super(solver, timeMillisSpent, newBestSolution, newBestScore.raw(), newBestScore.isFullyAssigned());
        this.solutionCloner = solutionCloner;
        this.unassignedCount = newBestScore.unassignedCount();
    }

    @Override
    public @NonNull Solution_ getNewBestSolution() {
        // We need to clone the new best solution, or we may end up sharing the same instance with consumers.
        // Reusing the instance can lead to inconsistent states if intermediary consumers change the solution.
        // We only do the cloning when the solution is requested - that way, we do not slow down the solver loop.
        if (clonedSolution == null) {
            synchronized (this) {
                if (clonedSolution == null) {
                    clonedSolution = solutionCloner.apply(super.getNewBestSolution());
                }
            }
        }
        return Objects.requireNonNull(clonedSolution, "Impossible state: solution cloner returned null.");
    }

    public int getUnassignedCount() {
        return unassignedCount;
    }

}
