package ai.timefold.solver.core.impl.score.stream.common.quad;

import static ai.timefold.solver.core.impl.util.ConstantLambdaUtils.quadPickFirst;
import static ai.timefold.solver.core.impl.util.ConstantLambdaUtils.quadPickFourth;
import static ai.timefold.solver.core.impl.util.ConstantLambdaUtils.quadPickSecond;
import static ai.timefold.solver.core.impl.util.ConstantLambdaUtils.quadPickThird;

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
            return groupBy(quadPickFirst(),
                    quadPickSecond(),
                    quadPickThird(),
                    quadPickFourth());
        }
    }

    @Override
    @NonNull
    default QuadConstraintStub<A, B, C, D> penalize() {
        return innerImpact(ScoreImpactType.PENALTY);
    }

    QuadConstraintStub<A, B, C, D> innerImpact(ScoreImpactType scoreImpactType);

    @Override
    @NonNull
    default QuadConstraintStub<A, B, C, D> reward() {
        return innerImpact(ScoreImpactType.REWARD);
    }

    @Override
    @NonNull
    default QuadConstraintStub<A, B, C, D> impact() {
        return innerImpact(ScoreImpactType.MIXED);
    }

    @Override
    default QuadConstraintBuilder<A, B, C, D, ?> penalizeConfigurable(ToIntQuadFunction<A, B, C, D> matchWeigher) {
        return penalize()
                .usingDefaultConstraintWeight(null);
    }

    @Override
    default QuadConstraintBuilder<A, B, C, D, ?> penalizeConfigurableLong(ToLongQuadFunction<A, B, C, D> matchWeigher) {
        return penalize()
                .usingDefaultConstraintWeight(null);
    }

    @Override
    default QuadConstraintBuilder<A, B, C, D, ?>
            penalizeConfigurableBigDecimal(QuadFunction<A, B, C, D, BigDecimal> matchWeigher) {
        return penalize()
                .usingDefaultConstraintWeight(null);
    }

    @Override
    default QuadConstraintBuilder<A, B, C, D, ?> rewardConfigurable(ToIntQuadFunction<A, B, C, D> matchWeigher) {
        return reward()
                .usingDefaultConstraintWeight(null);
    }

    @Override
    default QuadConstraintBuilder<A, B, C, D, ?> rewardConfigurableLong(ToLongQuadFunction<A, B, C, D> matchWeigher) {
        return reward()
                .usingDefaultConstraintWeight(null);
    }

    @Override
    default QuadConstraintBuilder<A, B, C, D, ?>
            rewardConfigurableBigDecimal(QuadFunction<A, B, C, D, BigDecimal> matchWeigher) {
        return reward()
                .usingDefaultConstraintWeight(null);
    }

    @Override
    default QuadConstraintBuilder<A, B, C, D, ?> impactConfigurable(ToIntQuadFunction<A, B, C, D> matchWeigher) {
        return impact()
                .usingDefaultConstraintWeight(null);
    }

    @Override
    default QuadConstraintBuilder<A, B, C, D, ?> impactConfigurableLong(ToLongQuadFunction<A, B, C, D> matchWeigher) {
        return impact()
                .usingDefaultConstraintWeight(null);
    }

    @Override
    default QuadConstraintBuilder<A, B, C, D, ?>
            impactConfigurableBigDecimal(QuadFunction<A, B, C, D, BigDecimal> matchWeigher) {
        return impact()
                .usingDefaultConstraintWeight(null);
    }

}
