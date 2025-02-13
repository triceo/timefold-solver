package ai.timefold.solver.core.api.score.stream.quad;

import java.util.Collection;

import ai.timefold.solver.core.api.function.PentaFunction;
import ai.timefold.solver.core.api.function.QuadFunction;
import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.ConstraintDefinition;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * As defined by {@link ConstraintDefinition},
 * specialized for constraints with four fact outputs.
 */
@NullMarked
public interface QuadConstraintDefinition<A, B, C, D, Score_ extends Score<Score_>>
        extends ConstraintDefinition<Score_> {

    @Override
    QuadConstraintStub<A, B, C, D, Score_> buildConstraint(ConstraintFactory constraintFactory);

    @Override
    default @Nullable PentaFunction<A, B, C, D, Score_, ConstraintJustification> justificationFunction() {
        return null;
    }

    @Override
    default @Nullable QuadFunction<A, B, C, D, Collection<Object>> indictmentFunction() {
        return null;
    }

}
