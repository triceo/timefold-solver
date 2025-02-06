package ai.timefold.solver.core.api.score.stream.tmp;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.ConstraintBuilder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface ConstraintStub<Score_ extends Score<Score_>> {

    @NonNull ConstraintBuilder usingDefaultConstraintWeight(@Nullable Score_ constraintWeight);

}
