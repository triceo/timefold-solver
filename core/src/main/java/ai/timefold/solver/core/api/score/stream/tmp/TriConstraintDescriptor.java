package ai.timefold.solver.core.api.score.stream.tmp;

import java.util.Collection;

import ai.timefold.solver.core.api.function.QuadFunction;
import ai.timefold.solver.core.api.function.TriFunction;
import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

public interface TriConstraintDescriptor<A, B, C, Score_ extends Score<Score_>>
        extends ConstraintDescriptor<Score_> {

    @Override
    TriConstraintStub<A, B, C, Score_> buildConstraint(ConstraintFactory constraintFactory);

    @Override
    default QuadFunction<A, B, C, Score_, ConstraintJustification> justificationFunction() {
        return null;
    }

    @Override
    default TriFunction<A, B, C, Collection<Object>> indictmentFunction() {
        return null;
    }

}
