package ai.timefold.solver.core.api.score.stream;

import java.util.Objects;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.uni.UniConstraintDefinition;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface ConstraintDefinition<Score_ extends Score<@NonNull Score_>> {

    ConstraintStub<Score_> buildConstraint(ConstraintFactory constraintFactory);

    Score_ defaultConstraintWeight();

    /**
     * Override this method together with {@link #justificationClass()} to collect constraint justifications.
     *
     * @return null if a default justification should be used;
     *         otherwise the expected return value depends on constraint stream cardinality.
     * @see UniConstraintDefinition#justificationFunction()
     *      Example of return value for a constraint stream of cardinality 1.
     */
    default @Nullable Object justificationFunction() {
        return null;
    }

    /**
     * Override this method together with {@link #justificationFunction()} to collect constraint justifications.
     * 
     * @return the constraint justification type being collected;
     *         the default return value is {@link DefaultConstraintJustification},
     *         which goes well with {@link #justificationFunction()} returning null.
     * @param <ConstraintJustification_> the actual type of the {@link ConstraintJustification},
     *        not known at compile time
     */
    @SuppressWarnings("unchecked")
    default <ConstraintJustification_ extends ConstraintJustification> Class<ConstraintJustification_>
            justificationClass() {
        return (Class<ConstraintJustification_>) DefaultConstraintJustification.class;
    }

    /**
     * @return null if default indictments should be used;
     *         otherwise the expected return value depends on constraint stream cardinality.
     * @see UniConstraintDefinition#indictmentFunction()
     *      Example of return value for a constraint stream of cardinality 1.
     */
    default @Nullable Object indictmentFunction() {
        return null;
    }

    /**
     * @return a name of the constraint;
     *         by default, the simple class name is taken and decamelized.
     *         If the class name ends with "Constraint" or "ConstraintDefinition",
     *         that suffix is removed first.
     */
    default String name() {
        var simpleName = getClass().getSimpleName();
        if (simpleName.endsWith("Constraint")) {
            simpleName = simpleName.substring(0, simpleName.length() - "Constraint".length());
        } else if (simpleName.endsWith("ConstraintDefinition")) {
            simpleName = simpleName.substring(0, simpleName.length() - "ConstraintDefinition".length());
        }
        return decamelize(simpleName);
    }

    /**
     * @return a description of the constraint, empty string by default
     */
    default String description() {
        return "";
    }

    /**
     * @return a group of the constraint, {@link Constraint#DEFAULT_CONSTRAINT_GROUP} by default;
     *         only allows alphanumeric characters, "-" and "_"
     */
    default String group() {
        return Constraint.DEFAULT_CONSTRAINT_GROUP;
    }

    private static String decamelize(String camelCaseText) {
        if (Objects.requireNonNull(camelCaseText).isEmpty()) {
            return "";
        }
        var result = new StringBuilder();
        result.append(Character.toUpperCase(camelCaseText.charAt(0)));
        for (var i = 1; i < camelCaseText.length(); i++) {
            char currentChar = camelCaseText.charAt(i);
            if (Character.isUpperCase(currentChar)) {
                result.append(' ');
                result.append(Character.toLowerCase(currentChar));
            } else {
                result.append(currentChar);
            }
        }
        return result.toString();
    }

}
