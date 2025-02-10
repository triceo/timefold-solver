package ai.timefold.solver.core.api.score.stream.quad;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.ConstraintStub;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface QuadConstraintStub<A, B, C, D, Score_ extends Score<Score_>> extends ConstraintStub<Score_> {

    @Override
    QuadConstraintBuilder<A, B, C, D, Score_> usingDefaultConstraintWeight(Score_ constraintWeight);

}
