package ai.timefold.solver.core.api.score.stream.uni;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.ConstraintStub;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface UniConstraintStub<A, Score_ extends Score<Score_>>
        extends ConstraintStub<Score_> {

    @Override
    UniConstraintBuilder<A, Score_> usingDefaultConstraintWeight(Score_ constraintWeight);

}
