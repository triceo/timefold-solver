package ai.timefold.solver.core.api.score.stream.tmp;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintJustification;
import ai.timefold.solver.core.api.score.stream.DefaultConstraintJustification;

public interface ConstraintDescriptor<Score_ extends Score<Score_>> {

    ConstraintStub<Score_> buildConstraint(ConstraintFactory constraintFactory);

    Score_ defaultConstraintWeight();

    default Object justificationFunction() {
        return null;
    }

    default <ConstraintJustification_ extends ConstraintJustification> Class<ConstraintJustification_> justificationType() {
        return (Class<ConstraintJustification_>) DefaultConstraintJustification.class;
    }

    default Object indictmentFunction() {
        return null;
    }

    default String name() {
        var simpleName = getClass().getSimpleName();
        if (simpleName.endsWith("Constraint")) {
            simpleName = simpleName.substring(0, simpleName.length() - "Constraint".length());
        } else if (simpleName.endsWith("ConstraintDefinition")) {
            simpleName = simpleName.substring(0, simpleName.length() - "ConstraintDefinition".length());
        }
        return Util.convertCamelCaseToSentence(simpleName);
    }

    default String description() {
        return "";
    }

    default String group() {
        return Constraint.DEFAULT_CONSTRAINT_GROUP;
    }

}
