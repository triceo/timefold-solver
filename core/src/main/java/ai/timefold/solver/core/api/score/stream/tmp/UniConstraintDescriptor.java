package ai.timefold.solver.core.api.score.stream.tmp;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

public interface UniConstraintDescriptor<A, Score_ extends Score<Score_>>
        extends ConstraintDescriptor<Score_> {

    @Override
    UniConstraintStub<A, Score_> buildConstraint(ConstraintFactory constraintFactory);

    @Override
    default BiFunction<A, Score_, ConstraintJustification> justificationFunction() {
        return null;
    }

    @Override
    default Function<A, Collection<Object>> indictmentFunction() {
        return null;
    }

}
