package ai.timefold.solver.core.api.score.stream.tri;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.ConstraintStub;

import org.jspecify.annotations.NullMarked;

/**
 * As defined by {@link ConstraintStub},
 * but specialized for constraint streams of cardinality 3 (tri-streams).
 */
@NullMarked
public interface TriConstraintStub<A, B, C, Score_ extends Score<Score_>> extends ConstraintStub<Score_> {

    @Override
    TriConstraintBuilder<A, B, C, Score_> usingDefaultConstraintWeight(Score_ constraintWeight);

}
