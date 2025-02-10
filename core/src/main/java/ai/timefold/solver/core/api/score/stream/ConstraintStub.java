package ai.timefold.solver.core.api.score.stream;

import ai.timefold.solver.core.api.score.Score;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ConstraintStub<Score_ extends Score<@NonNull Score_>> {

    ConstraintBuilder usingDefaultConstraintWeight(Score_ constraintWeight);

}
