package ai.timefold.solver.core.api.score.stream.tri;

import java.util.Collection;

import ai.timefold.solver.core.api.function.QuadFunction;
import ai.timefold.solver.core.api.function.TriFunction;
import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.ConstraintDefinition;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * As defined by {@link ConstraintDefinition},
 * specialized for constraints with three fact outputs.
 */
@NullMarked
public interface TriConstraintDefinition<A, B, C, Score_ extends Score<Score_>>
        extends ConstraintDefinition<Score_> {

    @Override
    TriConstraintStub<A, B, C, Score_> buildConstraint(ConstraintFactory constraintFactory);

    @Override
    default @Nullable QuadFunction<A, B, C, Score_, ConstraintJustification> justificationFunction() {
        return null;
    }

    @Override
    default @Nullable TriFunction<A, B, C, Collection<Object>> indictmentFunction() {
        return null;
    }

}
