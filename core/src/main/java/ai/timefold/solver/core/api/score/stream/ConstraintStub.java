package ai.timefold.solver.core.api.score.stream;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.bi.BiConstraintStub;
import ai.timefold.solver.core.api.score.stream.quad.QuadConstraintStub;
import ai.timefold.solver.core.api.score.stream.tri.TriConstraintStub;
import ai.timefold.solver.core.api.score.stream.uni.UniConstraintStub;

import org.jspecify.annotations.NullMarked;

/**
 * A stub for a constraint, allowing to configure its weights.
 * Specialized versions of this interface exist for different constraint stream cardinalities.
 * 
 * <p>
 * There are two modes to use this stub:
 * <ul>
 * <li>If using {@link ConstraintProvider}, call {@link #usingDefaultConstraintWeight(Score)},
 * continuing fluently from there to specify further details, such as constraint name.</li>
 * <li>
 * If using {@link ConstraintDefinition}, immediately return this stub.
 * Other constraint properties such as name etc. can be specified by overriding other methods on
 * {@link ConstraintDefinition}.</li>
 * </ul>
 *
 * @see UniConstraintStub Specialization for constraint streams with a single fact output.
 * @see BiConstraintStub Specialization for constraint streams with two fact outputs.
 * @see TriConstraintStub Specialization for constraint streams with three fact outputs.
 * @see QuadConstraintStub Specialization for constraint streams with four fact outputs.
 */
@NullMarked
public interface ConstraintStub {

    /**
     * The constraint will use this weight,
     * unless overridden using {@link ConstraintWeightOverrides}.
     * Do not call this method if used from {@link ConstraintDefinition#buildConstraint(ConstraintFactory)}.
     * 
     * @param constraintWeight null only for configurable constraint weights;
     *        since that feature is deprecated, user code should never pass null here.
     *        In future versions of Timefold Solver, a non-null value will be required.
     * @return fluent builder allowing to specify further details;
     *         continue fluently if using {@link ConstraintProvider},
     *         otherwise return this if using {@link ConstraintDefinition}.
     */
    <Score_ extends Score<Score_>> ConstraintBuilder usingDefaultConstraintWeight(Score_ constraintWeight);

}
