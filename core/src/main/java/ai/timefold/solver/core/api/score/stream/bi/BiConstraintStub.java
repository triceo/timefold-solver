package ai.timefold.solver.core.api.score.stream.bi;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.ConstraintStub;

import org.jspecify.annotations.NullMarked;

/**
 * As defined by {@link ConstraintStub},
 * but specialized for constraint streams of cardinality 2 (bi-streams).
 * 
 * @param <A>
 * @param <B>
 */
@NullMarked
public interface BiConstraintStub<A, B> extends ConstraintStub {

    @Override
    <Score_ extends Score<Score_>> BiConstraintBuilder<A, B, Score_> usingDefaultConstraintWeight(Score_ constraintWeight);

}
