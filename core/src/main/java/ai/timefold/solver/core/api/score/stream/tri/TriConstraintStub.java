package ai.timefold.solver.core.api.score.stream.tri;

import java.math.BigDecimal;

import ai.timefold.solver.core.api.function.ToIntTriFunction;
import ai.timefold.solver.core.api.function.ToLongTriFunction;
import ai.timefold.solver.core.api.function.TriFunction;
import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.buildin.bendable.BendableScore;
import ai.timefold.solver.core.api.score.buildin.bendablebigdecimal.BendableBigDecimalScore;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoftbigdecimal.HardMediumSoftBigDecimalScore;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScore;
import ai.timefold.solver.core.api.score.buildin.simple.SimpleScore;
import ai.timefold.solver.core.api.score.buildin.simplebigdecimal.SimpleBigDecimalScore;
import ai.timefold.solver.core.api.score.stream.ConstraintDefinition;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.ConstraintStub;

import org.jspecify.annotations.NullMarked;

/**
 * As defined by {@link ConstraintStub},
 * but specialized for constraint streams of cardinality 3 (tri-streams).
 * 
 * <p>
 * There are two modes to use this stub:
 *
 * <ul>
 * <li>If using {@link ConstraintProvider}, optionally set match weights
 * and then use {@link #usingDefaultConstraintWeight(Score)},
 * continuing fluently from there to specify further details, such as constraint name.</li>
 * <li>
 * If using {@link ConstraintDefinition}, optionally set match weights and then return this stub.
 * Other constraint properties such as name etc. can be specified by overriding other methods on
 * {@link ConstraintDefinition}.</li>
 * </ul>
 * 
 * <p>
 * The default match weight for constraints is {@code 1 (one)}.
 * To configure a different match weight function, use any one (and only one) of these methods:
 *
 * <ul>
 * <li>{@link #withMatchWeight(ToIntTriFunction)},
 * applicable to all {@link Score} types.</li>
 * <li>{@link #withLongMatchWeight(ToLongTriFunction)},
 * applicable to all {@link Score} types except
 * {@link SimpleScore}, {@link HardSoftScore}, {@link HardMediumSoftScore} and {@link BendableScore}.</li>
 * <li>{@link #withBigDecimalMatchWeight(TriFunction)},
 * only applicable to {@link BigDecimal BigDecimal-based} {@link Score} types
 * ({@link SimpleBigDecimalScore}, {@link HardSoftBigDecimalScore}, {@link HardMediumSoftBigDecimalScore},
 * {@link BendableBigDecimalScore}).</li>
 * </ul>
 */
@NullMarked
public interface TriConstraintStub<A, B, C> extends ConstraintStub {

    /**
     * Configure the constraint to use a match weight function.
     *
     * @param matchWeigher the result of this function (match weight) is multiplied by the constraint weight
     * @throws IllegalStateException if {@link #withLongMatchWeight(ToLongTriFunction)} or
     *         {@link #withBigDecimalMatchWeight(TriFunction)} was called before.
     * @return this
     */
    TriConstraintStub<A, B, C> withMatchWeight(ToIntTriFunction<A, B, C> matchWeigher);

    /**
     * Configure the constraint to use a match weight function.
     *
     * @param matchWeigher the result of this function (match weight) is multiplied by the constraint weight
     * @throws IllegalStateException if {@link #withMatchWeight(ToIntTriFunction)} or
     *         {@link #withBigDecimalMatchWeight(TriFunction)} was called before.
     * @return this
     */
    TriConstraintStub<A, B, C> withLongMatchWeight(ToLongTriFunction<A, B, C> matchWeigher);

    /**
     * Configure the constraint to use a match weight function.
     *
     * @param matchWeigher the result of this function (match weight) is multiplied by the constraint weight
     * @throws IllegalStateException if {@link #withMatchWeight(ToIntTriFunction)} or
     *         {@link #withLongMatchWeight(ToLongTriFunction)} was called before.
     * @return this
     */
    TriConstraintStub<A, B, C> withBigDecimalMatchWeight(TriFunction<A, B, C, BigDecimal> matchWeigher);

    @Override
    <Score_ extends Score<Score_>> TriConstraintBuilder<A, B, C, Score_> usingDefaultConstraintWeight(Score_ constraintWeight);

}
