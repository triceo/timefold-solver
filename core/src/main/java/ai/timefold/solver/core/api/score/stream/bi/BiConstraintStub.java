package ai.timefold.solver.core.api.score.stream.bi;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.ConstraintStub;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface BiConstraintStub<A, B, Score_ extends Score<Score_>> extends ConstraintStub<Score_> {

    @Override
    BiConstraintBuilder<A, B, Score_> usingDefaultConstraintWeight(Score_ constraintWeight);

}
