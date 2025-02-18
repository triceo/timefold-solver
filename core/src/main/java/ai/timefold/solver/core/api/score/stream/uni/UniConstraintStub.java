package ai.timefold.solver.core.api.score.stream.uni;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.ConstraintStub;

import org.jspecify.annotations.NullMarked;

/**
 * As defined by {@link ConstraintStub},
 * but specialized for constraint streams of cardinality 1 (uni-streams).
 *
 * @param <A>
 */
@NullMarked
public interface UniConstraintStub<A> extends ConstraintStub {

    @Override
    <Score_ extends Score<Score_>> UniConstraintBuilder<A, Score_> usingDefaultConstraintWeight(Score_ constraintWeight);

}
