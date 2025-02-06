package ai.timefold.solver.core.api.score.stream.tmp;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.tri.TriConstraintBuilder;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface TriConstraintStub<A, B, C, Score_ extends Score<Score_>> extends ConstraintStub<Score_>{

    @Override
    @NonNull
    TriConstraintBuilder<A, B, C, Score_> usingDefaultConstraintWeight(@Nullable Score_ constraintWeight);

}
