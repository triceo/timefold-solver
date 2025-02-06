package ai.timefold.solver.core.api.score.stream.tmp;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.quad.QuadConstraintBuilder;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface QuadConstraintStub<A, B, C, D, Score_ extends Score<Score_>> extends ConstraintStub<Score_> {

    @Override
    @NonNull
    QuadConstraintBuilder<A, B, C, D, Score_> usingDefaultConstraintWeight(@Nullable Score_ constraintWeight);

}
