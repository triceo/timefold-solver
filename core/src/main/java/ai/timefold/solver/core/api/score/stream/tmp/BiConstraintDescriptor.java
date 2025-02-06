package ai.timefold.solver.core.api.score.stream.tmp;

import java.util.Collection;
import java.util.function.BiFunction;

import ai.timefold.solver.core.api.function.TriFunction;
import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

public interface BiConstraintDescriptor<A, B, Score_ extends Score<Score_>>
        extends ConstraintDescriptor<Score_> {

    @Override
    BiConstraintStub<A, B, Score_> buildConstraint(ConstraintFactory constraintFactory);

    @Override
    default TriFunction<A, B, Score_, ConstraintJustification> justificationFunction() {
        return null;
    }

    @Override
    default BiFunction<A, B, Collection<Object>> indictmentFunction() {
        return null;
    }

}
