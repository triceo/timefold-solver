package ai.timefold.solver.core.impl.score.stream.common.quad;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;

import ai.timefold.solver.core.api.function.PentaFunction;
import ai.timefold.solver.core.api.function.QuadFunction;
import ai.timefold.solver.core.api.function.ToIntQuadFunction;
import ai.timefold.solver.core.api.function.ToLongQuadFunction;
import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.DefaultConstraintJustification;
import ai.timefold.solver.core.api.score.stream.penta.PentaJoiner;
import ai.timefold.solver.core.api.score.stream.quad.QuadConstraintBuilder;
import ai.timefold.solver.core.api.score.stream.quad.QuadConstraintStream;
import ai.timefold.solver.core.api.score.stream.quad.QuadConstraintStub;
import ai.timefold.solver.core.impl.score.stream.common.RetrievalSemantics;
import ai.timefold.solver.core.impl.score.stream.common.ScoreImpactType;
import ai.timefold.solver.core.impl.util.ConstantLambdaUtils;

import org.jspecify.annotations.NonNull;

public interface InnerQuadConstraintStream<A, B, C, D> extends QuadConstraintStream<A, B, C, D> {

    static <A, B, C, D> PentaFunction<A, B, C, D, Score<?>, DefaultConstraintJustification>
            createDefaultJustificationMapping() {
        return (a, b, c, d, score) -> DefaultConstraintJustification.of(score, a, b, c, d);
    }

    static <A, B, C, D> QuadFunction<A, B, C, D, Collection<?>> createDefaultIndictedObjectsMapping() {
        return Arrays::asList;
    }

    RetrievalSemantics getRetrievalSemantics();

    /**
     * This method will return true if the constraint stream is guaranteed to only produce distinct tuples.
     * See {@link #distinct()} for details.
     *
     * @return true if the guarantee of distinct tuples is provided
     */
    boolean guaranteesDistinct();

    @Override
    default @NonNull <E> QuadConstraintStream<A, B, C, D> ifExists(@NonNull Class<E> otherClass,
            @NonNull PentaJoiner<A, B, C, D, E> @NonNull... joiners) {
        if (getRetrievalSemantics() == RetrievalSemantics.STANDARD) {
            return ifExists(getConstraintFactory().forEach(otherClass), joiners);
        } else {
            // Calls fromUnfiltered() for backward compatibility only
            return ifExists(getConstraintFactory().fromUnfiltered(otherClass), joiners);
        }
    }

    @Override
    default @NonNull <E> QuadConstraintStream<A, B, C, D> ifExistsIncludingUnassigned(@NonNull Class<E> otherClass,
            @NonNull PentaJoiner<A, B, C, D, E> @NonNull... joiners) {
        if (getRetrievalSemantics() == RetrievalSemantics.STANDARD) {
            return ifExists(getConstraintFactory().forEachIncludingUnassigned(otherClass), joiners);
        } else {
            return ifExists(getConstraintFactory().fromUnfiltered(otherClass), joiners);
        }
    }

    @Override
    default @NonNull <E> QuadConstraintStream<A, B, C, D> ifNotExists(@NonNull Class<E> otherClass,
            @NonNull PentaJoiner<A, B, C, D, E> @NonNull... joiners) {
        if (getRetrievalSemantics() == RetrievalSemantics.STANDARD) {
            return ifNotExists(getConstraintFactory().forEach(otherClass), joiners);
        } else {
            // Calls fromUnfiltered() for backward compatibility only
            return ifNotExists(getConstraintFactory().fromUnfiltered(otherClass), joiners);
        }
    }

    @Override
    default @NonNull <E> QuadConstraintStream<A, B, C, D> ifNotExistsIncludingUnassigned(@NonNull Class<E> otherClass,
            @NonNull PentaJoiner<A, B, C, D, E> @NonNull... joiners) {
        if (getRetrievalSemantics() == RetrievalSemantics.STANDARD) {
            return ifNotExists(getConstraintFactory().forEachIncludingUnassigned(otherClass), joiners);
        } else {
            return ifNotExists(getConstraintFactory().fromUnfiltered(otherClass), joiners);
        }
    }

    @Override
    default @NonNull QuadConstraintStream<A, B, C, D> distinct() {
        if (guaranteesDistinct()) {
            return this;
        } else {
            return groupBy(ConstantLambdaUtils.quadPickFirst(),
                    ConstantLambdaUtils.quadPickSecond(),
                    ConstantLambdaUtils.quadPickThird(),
                    ConstantLambdaUtils.quadPickFourth());
        }
    }

    @Override
    @NonNull
    default <Score_ extends @NonNull Score<Score_>> QuadConstraintStub<A, B, C, D, Score_>
            penalize(ToIntQuadFunction<A, B, C, D> matchWeigher) {
        return innerImpact(matchWeigher, ScoreImpactType.PENALTY);
    }

    @Override
    @NonNull
    default <Score_ extends @NonNull Score<Score_>> QuadConstraintStub<A, B, C, D, Score_>
            penalizeLong(ToLongQuadFunction<A, B, C, D> matchWeigher) {
        return innerImpact(matchWeigher, ScoreImpactType.PENALTY);
    }

    @Override
    @NonNull
    default <Score_ extends @NonNull Score<Score_>> QuadConstraintStub<A, B, C, D, Score_>
            penalizeBigDecimal(QuadFunction<A, B, C, D, BigDecimal> matchWeigher) {
        return innerImpact(matchWeigher, ScoreImpactType.PENALTY);
    }

    @Override
    @NonNull
    default <Score_ extends @NonNull Score<Score_>> QuadConstraintStub<A, B, C, D, Score_>
            rewardLong(ToLongQuadFunction<A, B, C, D> matchWeigher) {
        return innerImpact(matchWeigher, ScoreImpactType.REWARD);
    }

    @Override
    @NonNull
    default <Score_ extends @NonNull Score<Score_>> QuadConstraintStub<A, B, C, D, Score_>
            rewardBigDecimal(QuadFunction<A, B, C, D, BigDecimal> matchWeigher) {
        return innerImpact(matchWeigher, ScoreImpactType.REWARD);
    }

    @Override
    @NonNull
    default <Score_ extends @NonNull Score<Score_>> QuadConstraintStub<A, B, C, D, Score_>
            reward(ToIntQuadFunction<A, B, C, D> matchWeigher) {
        return innerImpact(matchWeigher, ScoreImpactType.REWARD);
    }

    @Override
    @NonNull
    default <Score_ extends @NonNull Score<Score_>> QuadConstraintStub<A, B, C, D, Score_>
            impact(ToIntQuadFunction<A, B, C, D> matchWeigher) {
        return innerImpact(matchWeigher, ScoreImpactType.MIXED);
    }

    @Override
    @NonNull
    default <Score_ extends @NonNull Score<Score_>> QuadConstraintStub<A, B, C, D, Score_>
            impactLong(ToLongQuadFunction<A, B, C, D> matchWeigher) {
        return innerImpact(matchWeigher, ScoreImpactType.MIXED);
    }

    @Override
    @NonNull
    default <Score_ extends @NonNull Score<Score_>> QuadConstraintStub<A, B, C, D, Score_>
            impactBigDecimal(QuadFunction<A, B, C, D, BigDecimal> matchWeigher) {
        return innerImpact(matchWeigher, ScoreImpactType.MIXED);
    }

    @Override
    default QuadConstraintBuilder<A, B, C, D, ?> penalizeConfigurable(ToIntQuadFunction<A, B, C, D> matchWeigher) {
        return innerImpact(matchWeigher, ScoreImpactType.PENALTY).usingDefaultConstraintWeight(null);
    }

    @Override
    default QuadConstraintBuilder<A, B, C, D, ?> penalizeConfigurableLong(ToLongQuadFunction<A, B, C, D> matchWeigher) {
        return innerImpact(matchWeigher, ScoreImpactType.PENALTY).usingDefaultConstraintWeight(null);
    }

    @Override
    default QuadConstraintBuilder<A, B, C, D, ?>
            penalizeConfigurableBigDecimal(QuadFunction<A, B, C, D, BigDecimal> matchWeigher) {
        return innerImpact(matchWeigher, ScoreImpactType.PENALTY).usingDefaultConstraintWeight(null);
    }

    @Override
    default QuadConstraintBuilder<A, B, C, D, ?> rewardConfigurable(ToIntQuadFunction<A, B, C, D> matchWeigher) {
        return innerImpact(matchWeigher, ScoreImpactType.REWARD).usingDefaultConstraintWeight(null);
    }

    @Override
    default QuadConstraintBuilder<A, B, C, D, ?> rewardConfigurableLong(ToLongQuadFunction<A, B, C, D> matchWeigher) {
        return innerImpact(matchWeigher, ScoreImpactType.REWARD).usingDefaultConstraintWeight(null);
    }

    @Override
    default QuadConstraintBuilder<A, B, C, D, ?>
            rewardConfigurableBigDecimal(QuadFunction<A, B, C, D, BigDecimal> matchWeigher) {
        return innerImpact(matchWeigher, ScoreImpactType.REWARD).usingDefaultConstraintWeight(null);
    }

    @Override
    default QuadConstraintBuilder<A, B, C, D, ?> impactConfigurable(ToIntQuadFunction<A, B, C, D> matchWeigher) {
        return innerImpact(matchWeigher, ScoreImpactType.MIXED).usingDefaultConstraintWeight(null);
    }

    @Override
    default QuadConstraintBuilder<A, B, C, D, ?> impactConfigurableLong(ToLongQuadFunction<A, B, C, D> matchWeigher) {
        return innerImpact(matchWeigher, ScoreImpactType.MIXED).usingDefaultConstraintWeight(null);
    }

    @Override
    default QuadConstraintBuilder<A, B, C, D, ?>
            impactConfigurableBigDecimal(QuadFunction<A, B, C, D, BigDecimal> matchWeigher) {
        return innerImpact(matchWeigher, ScoreImpactType.MIXED).usingDefaultConstraintWeight(null);
    }

    <Score_ extends Score<Score_>> QuadConstraintStub<A, B, C, D, Score_>
            innerImpact(ToIntQuadFunction<A, B, C, D> matchWeigher, ScoreImpactType scoreImpactType);

    <Score_ extends Score<Score_>> QuadConstraintStub<A, B, C, D, Score_>
            innerImpact(ToLongQuadFunction<A, B, C, D> matchWeigher, ScoreImpactType scoreImpactType);

    <Score_ extends Score<Score_>> QuadConstraintStub<A, B, C, D, Score_>
            innerImpact(QuadFunction<A, B, C, D, BigDecimal> matchWeigher, ScoreImpactType scoreImpactType);

}
