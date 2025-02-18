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
    default QuadConstraintStub<A, B, C, D> penalizeWeighted(@NonNull ToIntQuadFunction<A, B, C, D> matchWeigher) {
        return innerImpact(matchWeigher, ScoreImpactType.PENALTY);
    }

    QuadConstraintStub<A, B, C, D> innerImpact(ToIntQuadFunction<A, B, C, D> matchWeigher, ScoreImpactType scoreImpactType);

    @Override
    @NonNull
    default QuadConstraintStub<A, B, C, D> penalizeWeightedLong(@NonNull ToLongQuadFunction<A, B, C, D> matchWeigher) {
        return innerImpactLong(matchWeigher, ScoreImpactType.PENALTY);
    }

    QuadConstraintStub<A, B, C, D> innerImpactLong(ToLongQuadFunction<A, B, C, D> matchWeigher,
            ScoreImpactType scoreImpactType);

    @Override
    @NonNull
    default QuadConstraintStub<A, B, C, D>
            penalizeWeightedBigDecimal(@NonNull QuadFunction<A, B, C, D, BigDecimal> matchWeigher) {
        return innerImpactBigDecimal(matchWeigher, ScoreImpactType.PENALTY);
    }

    QuadConstraintStub<A, B, C, D> innerImpactBigDecimal(QuadFunction<A, B, C, D, BigDecimal> matchWeigher,
            ScoreImpactType scoreImpactType);

    @Override
    @NonNull
    default QuadConstraintStub<A, B, C, D> rewardWeighted(@NonNull ToIntQuadFunction<A, B, C, D> matchWeigher) {
        return innerImpact(matchWeigher, ScoreImpactType.REWARD);
    }

    @Override
    @NonNull
    default QuadConstraintStub<A, B, C, D> rewardWeightedLong(@NonNull ToLongQuadFunction<A, B, C, D> matchWeigher) {
        return innerImpactLong(matchWeigher, ScoreImpactType.REWARD);
    }

    @Override
    @NonNull
    default QuadConstraintStub<A, B, C, D>
            rewardWeightedBigDecimal(@NonNull QuadFunction<A, B, C, D, BigDecimal> matchWeigher) {
        return innerImpactBigDecimal(matchWeigher, ScoreImpactType.REWARD);
    }

    @Override
    @NonNull
    default QuadConstraintStub<A, B, C, D> impactWeighted(@NonNull ToIntQuadFunction<A, B, C, D> matchWeigher) {
        return innerImpact(matchWeigher, ScoreImpactType.MIXED);
    }

    @Override
    @NonNull
    default QuadConstraintStub<A, B, C, D> impactWeightedLong(@NonNull ToLongQuadFunction<A, B, C, D> matchWeigher) {
        return innerImpactLong(matchWeigher, ScoreImpactType.MIXED);
    }

    @Override
    @NonNull
    default QuadConstraintStub<A, B, C, D>
            impactWeightedBigDecimal(@NonNull QuadFunction<A, B, C, D, BigDecimal> matchWeigher) {
        return innerImpactBigDecimal(matchWeigher, ScoreImpactType.MIXED);
    }

}
