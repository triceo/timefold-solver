package ai.timefold.solver.core.api.score.stream.tmp;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.uni.UniConstraintBuilder;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface UniConstraintStub<A, Score_ extends Score<Score_>>
        extends ConstraintStub<Score_> {

    @Override
    @NonNull
    UniConstraintBuilder<A, Score_> usingDefaultConstraintWeight(@Nullable Score_ constraintWeight);

}
