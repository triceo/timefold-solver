package ai.timefold.solver.core.api.score.stream;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.score.Score;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;

/**
 * A stub for a constraint, allowing to configure its weight.
 * There are two modes to use this stub:
 * 
 * <ul>
 * <li>If using {@link ConstraintProvider}, use {@link #usingDefaultConstraintWeight(Score)}
 * and continue fluently to specify further details, such as constraint name.</li>
 * <li>
 * If using {@link ConstraintDefinition}, use {@link #usingDefaultConstraintWeight(Score)}
 * and immediately return its return value.
 * Other constraint properties such as name etc. can be specified by overriding other methods on
 * {@link ConstraintDefinition}.</li>
 * </ul>
 * 
 * @param <Score_> the type of the score
 */
@NullMarked
public interface ConstraintStub<Score_ extends Score<@NonNull Score_>> {

    /**
     * The constraint will use this weight,
     * unless overridden using {@link ConstraintWeightOverrides}
     * 
     * @param constraintWeight null only for configurable constraint weights;
     *        since that feature is deprecated, user code should never pass null here.
     * @return fluent builder allowing to specify further details;
     *         continue fluently if using {@link ConstraintProvider},
     *         otherwise return this if using {@link ConstraintDefinition}.
     */
    ConstraintBuilder usingDefaultConstraintWeight(Score_ constraintWeight);

}
