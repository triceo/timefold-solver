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
 * @param <Score_>
 */
@NullMarked
public interface BiConstraintStub<A, B, Score_ extends Score<Score_>> extends ConstraintStub<Score_> {

    @Override
    BiConstraintBuilder<A, B, Score_> usingDefaultConstraintWeight(Score_ constraintWeight);

}
