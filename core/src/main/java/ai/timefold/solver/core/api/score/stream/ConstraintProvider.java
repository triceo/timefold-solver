package ai.timefold.solver.core.api.score.stream;

import ai.timefold.solver.core.api.domain.constraintweight.ConstraintWeight;
import ai.timefold.solver.core.api.score.Score;

import org.jspecify.annotations.NullMarked;

/**
 * Used by Constraint Streams' {@link Score} calculation.
 * An implementation must be stateless in order to facilitate building a single set of constraints
 * independent of potentially changing constraint weights.
 *
 * @see ComposableConstraintProvider Composing constraints.
 */
@NullMarked
public interface ConstraintProvider {

    /**
     * This method is called once to create the constraints.
     * To create a {@link Constraint}, start with {@link ConstraintFactory#forEach(Class)}.
     *
     * @return an array of all {@link Constraint constraints} that could apply.
     *         The constraints with a zero {@link ConstraintWeight} for a particular problem
     *         will be automatically disabled when scoring that problem, to improve performance.
     */
    Constraint[] defineConstraints(ConstraintFactory constraintFactory);

}
