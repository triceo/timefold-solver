package ai.timefold.solver.core.api.score.stream.tmp;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.bi.BiConstraintBuilder;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface BiConstraintStub<A, B, Score_ extends Score<Score_>> extends ConstraintStub<Score_> {

    @Override
    @NonNull
    BiConstraintBuilder<A, B, Score_> usingDefaultConstraintWeight(@Nullable Score_ constraintWeight);

}
