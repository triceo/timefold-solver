package ai.timefold.solver.core.api.score.stream.tmp;

import java.util.Collection;

import ai.timefold.solver.core.api.function.PentaFunction;
import ai.timefold.solver.core.api.function.QuadFunction;
import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

public interface QuadConstraintDescriptor<A, B, C, D, Score_ extends Score<Score_>>
        extends
        ConstraintDescriptor<Score_> {

    @Override
    QuadConstraintStub<A, B, C, D, Score_> buildConstraint(ConstraintFactory constraintFactory);

    @Override
    default PentaFunction<A, B, C, D, Score_, ConstraintJustification> justificationFunction() {
        return null;
    }

    @Override
    default QuadFunction<A, B, C, D, Collection<Object>> indictmentFunction() {
        return null;
    }

}
