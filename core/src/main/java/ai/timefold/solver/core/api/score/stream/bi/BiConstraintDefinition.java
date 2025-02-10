package ai.timefold.solver.core.api.score.stream.bi;

import java.util.Collection;
import java.util.function.BiFunction;

import ai.timefold.solver.core.api.function.TriFunction;
import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.ConstraintDefinition;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface BiConstraintDefinition<A, B, Score_ extends Score<Score_>>
        extends ConstraintDefinition<Score_> {

    @Override
    BiConstraintStub<A, B, Score_> buildConstraint(ConstraintFactory constraintFactory);

    @Override
    default @Nullable TriFunction<A, B, Score_, ConstraintJustification> justificationFunction() {
        return null;
    }

    @Override
    default @Nullable BiFunction<A, B, Collection<Object>> indictmentFunction() {
        return null;
    }

}
