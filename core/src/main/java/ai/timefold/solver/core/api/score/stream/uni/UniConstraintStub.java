package ai.timefold.solver.core.api.score.stream.uni;

import java.math.BigDecimal;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

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
 * but specialized for constraint streams of cardinality 1 (uni-streams).
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
 * <li>{@link #withMatchWeight(ToIntFunction)},
 * applicable to all {@link Score} types.</li>
 * <li>{@link #withLongMatchWeight(ToLongFunction)},
 * applicable to all {@link Score} types except
 * {@link SimpleScore}, {@link HardSoftScore}, {@link HardMediumSoftScore} and {@link BendableScore}.</li>
 * <li>{@link #withBigDecimalMatchWeight(Function)},
 * only applicable to {@link BigDecimal BigDecimal-based} {@link Score} types
 * ({@link SimpleBigDecimalScore}, {@link HardSoftBigDecimalScore}, {@link HardMediumSoftBigDecimalScore},
 * {@link BendableBigDecimalScore}).</li>
 * </ul>
 */
@NullMarked
public interface UniConstraintStub<A> extends ConstraintStub {

    /**
     * Configure the constraint to use a match weight function.
     * 
     * @param matchWeigher the result of this function (match weight) is multiplied by the constraint weight
     * @throws IllegalStateException if {@link #withLongMatchWeight(ToLongFunction)} or
     *         {@link #withBigDecimalMatchWeight(Function)} was called before.
     * @return this
     */
    UniConstraintStub<A> withMatchWeight(ToIntFunction<A> matchWeigher);

    /**
     * Configure the constraint to use a match weight function.
     *
     * @param matchWeigher the result of this function (match weight) is multiplied by the constraint weight
     * @throws IllegalStateException if {@link #withMatchWeight(ToIntFunction)} or
     *         {@link #withBigDecimalMatchWeight(Function)} was called before.
     * @return this
     */
    UniConstraintStub<A> withLongMatchWeight(ToLongFunction<A> matchWeigher);

    /**
     * Configure the constraint to use a match weight function.
     *
     * @param matchWeigher the result of this function (match weight) is multiplied by the constraint weight
     * @throws IllegalStateException if {@link #withMatchWeight(ToIntFunction)} or
     *         {@link #withLongMatchWeight(ToLongFunction)} was called before.
     * @return this
     */
    UniConstraintStub<A> withBigDecimalMatchWeight(Function<A, BigDecimal> matchWeigher);

    @Override
    <Score_ extends Score<Score_>> UniConstraintBuilder<A, Score_> usingDefaultConstraintWeight(Score_ constraintWeight);

}
