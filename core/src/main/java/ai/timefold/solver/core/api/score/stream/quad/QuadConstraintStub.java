package ai.timefold.solver.core.api.score.stream.quad;

import java.math.BigDecimal;

import ai.timefold.solver.core.api.function.QuadFunction;
import ai.timefold.solver.core.api.function.ToIntQuadFunction;
import ai.timefold.solver.core.api.function.ToLongQuadFunction;
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
 * but specialized for constraint streams of cardinality 4 (quad-streams).
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
 * <li>{@link #withMatchWeight(ToIntQuadFunction)},
 * applicable to all {@link Score} types.</li>
 * <li>{@link #withLongMatchWeight(ToLongQuadFunction)},
 * applicable to all {@link Score} types except
 * {@link SimpleScore}, {@link HardSoftScore}, {@link HardMediumSoftScore} and {@link BendableScore}.</li>
 * <li>{@link #withBigDecimalMatchWeight(QuadFunction)},
 * only applicable to {@link BigDecimal BigDecimal-based} {@link Score} types
 * ({@link SimpleBigDecimalScore}, {@link HardSoftBigDecimalScore}, {@link HardMediumSoftBigDecimalScore},
 * {@link BendableBigDecimalScore}).</li>
 * </ul>
 */
@NullMarked
public interface QuadConstraintStub<A, B, C, D> extends ConstraintStub {

    /**
     * Configure the constraint to use a match weight function.
     *
     * @param matchWeigher the result of this function (match weight) is multiplied by the constraint weight
     * @throws IllegalStateException if {@link #withLongMatchWeight(ToLongQuadFunction)} or
     *         {@link #withBigDecimalMatchWeight(QuadFunction)} was called before.
     * @return this
     */
    QuadConstraintStub<A, B, C, D> withMatchWeight(ToIntQuadFunction<A, B, C, D> matchWeigher);

    /**
     * Configure the constraint to use a match weight function.
     *
     * @param matchWeigher the result of this function (match weight) is multiplied by the constraint weight
     * @throws IllegalStateException if {@link #withMatchWeight(ToIntQuadFunction)} or
     *         {@link #withBigDecimalMatchWeight(QuadFunction)} was called before.
     * @return this
     */
    QuadConstraintStub<A, B, C, D> withLongMatchWeight(ToLongQuadFunction<A, B, C, D> matchWeigher);

    /**
     * Configure the constraint to use a match weight function.
     *
     * @param matchWeigher the result of this function (match weight) is multiplied by the constraint weight
     * @throws IllegalStateException if {@link #withMatchWeight(ToIntQuadFunction)} or
     *         {@link #withLongMatchWeight(ToLongQuadFunction)} was called before.
     * @return this
     */
    QuadConstraintStub<A, B, C, D> withBigDecimalMatchWeight(QuadFunction<A, B, C, D, BigDecimal> matchWeigher);

    @Override
    <Score_ extends Score<Score_>> QuadConstraintBuilder<A, B, C, D, Score_>
            usingDefaultConstraintWeight(Score_ constraintWeight);

}
