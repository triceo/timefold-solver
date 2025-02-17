package ai.timefold.solver.core.api.score.stream.uni;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.ConstraintDefinition;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * As defined by {@link ConstraintDefinition},
 * specialized for constraints with a single fact output.
 */
@NullMarked
public interface UniConstraintDefinition<A, Score_ extends Score<Score_>>
        extends ConstraintDefinition<Score_> {

    @Override
    UniConstraintStub<A> buildConstraint(ConstraintFactory constraintFactory);

    @Override
    default @Nullable BiFunction<A, Score_, ConstraintJustification> justificationFunction() {
        return null;
    }

    @Override
    default @Nullable Function<A, Collection<Object>> indictmentFunction() {
        return null;
    }

}
