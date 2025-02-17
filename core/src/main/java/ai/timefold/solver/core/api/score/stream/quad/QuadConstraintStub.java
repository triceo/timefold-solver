package ai.timefold.solver.core.api.score.stream.quad;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.ConstraintStub;

import org.jspecify.annotations.NullMarked;

/**
 * As defined by {@link ConstraintStub},
 * but specialized for constraint streams of cardinality 4 (quad-streams).
 */
@NullMarked
public interface QuadConstraintStub<A, B, C, D> extends ConstraintStub {

    @Override
    <Score_ extends Score<Score_>> QuadConstraintBuilder<A, B, C, D, Score_>
            usingDefaultConstraintWeight(Score_ constraintWeight);

}
