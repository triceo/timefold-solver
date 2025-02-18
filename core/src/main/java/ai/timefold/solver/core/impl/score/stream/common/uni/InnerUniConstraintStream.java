package ai.timefold.solver.core.impl.score.stream.common.uni;

import static ai.timefold.solver.core.impl.score.stream.common.RetrievalSemantics.STANDARD;
import static ai.timefold.solver.core.impl.util.ConstantLambdaUtils.identity;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.DefaultConstraintJustification;
import ai.timefold.solver.core.api.score.stream.bi.BiConstraintStream;
import ai.timefold.solver.core.api.score.stream.bi.BiJoiner;
import ai.timefold.solver.core.api.score.stream.uni.UniConstraintStream;
import ai.timefold.solver.core.api.score.stream.uni.UniConstraintStub;
import ai.timefold.solver.core.impl.bavet.bi.joiner.BiJoinerComber;
import ai.timefold.solver.core.impl.score.stream.common.RetrievalSemantics;
import ai.timefold.solver.core.impl.score.stream.common.ScoreImpactType;

import org.jspecify.annotations.NonNull;

public interface InnerUniConstraintStream<A> extends UniConstraintStream<A> {

    static <A> BiFunction<A, Score<?>, DefaultConstraintJustification> createDefaultJustificationMapping() {
        return (a, score) -> DefaultConstraintJustification.of(score, a);
    }

    static <A> Function<A, Collection<?>> createDefaultIndictedObjectsMapping() {
        return Collections::singletonList;
    }

    RetrievalSemantics getRetrievalSemantics();

    /**
     * This method returns true if the constraint stream is guaranteed to only produce distinct tuples.
     * See {@link #distinct()} for details.
     *
     * @return true if the guarantee of distinct tuples is provided
     */
    boolean guaranteesDistinct();

    @Override
    default @NonNull <B> BiConstraintStream<A, B> join(@NonNull Class<B> otherClass, @NonNull BiJoiner<A, B>... joiners) {
        if (getRetrievalSemantics() == STANDARD) {
            return join(getConstraintFactory().forEach(otherClass), joiners);
        } else {
            return join(getConstraintFactory().from(otherClass), joiners);
        }
    }

    /**
     * Allows {@link ConstraintFactory#forEachUniquePair(Class)} to reuse the joiner combing logic.
     *
     * @param otherStream never null
     * @param joinerComber never null
     * @param <B>
     * @return never null
     */
    <B> BiConstraintStream<A, B> join(UniConstraintStream<B> otherStream, BiJoinerComber<A, B> joinerComber);

    @Override
    default @NonNull <B> UniConstraintStream<A> ifExists(@NonNull Class<B> otherClass, @NonNull BiJoiner<A, B>... joiners) {
        if (getRetrievalSemantics() == STANDARD) {
            return ifExists(getConstraintFactory().forEach(otherClass), joiners);
        } else {
            // Calls fromUnfiltered() for backward compatibility only
            return ifExists(getConstraintFactory().fromUnfiltered(otherClass), joiners);
        }
    }

    @Override
    default @NonNull <B> UniConstraintStream<A> ifExistsIncludingUnassigned(@NonNull Class<B> otherClass,
            @NonNull BiJoiner<A, B>... joiners) {
        if (getRetrievalSemantics() == STANDARD) {
            return ifExists(getConstraintFactory().forEachIncludingUnassigned(otherClass), joiners);
        } else {
            return ifExists(getConstraintFactory().fromUnfiltered(otherClass), joiners);
        }
    }

    @Override
    default @NonNull <B> UniConstraintStream<A> ifNotExists(@NonNull Class<B> otherClass, @NonNull BiJoiner<A, B>... joiners) {
        if (getRetrievalSemantics() == STANDARD) {
            return ifNotExists(getConstraintFactory().forEach(otherClass), joiners);
        } else {
            // Calls fromUnfiltered() for backward compatibility only
            return ifNotExists(getConstraintFactory().fromUnfiltered(otherClass), joiners);
        }
    }

    @Override
    default @NonNull <B> UniConstraintStream<A> ifNotExistsIncludingUnassigned(@NonNull Class<B> otherClass,
            @NonNull BiJoiner<A, B>... joiners) {
        if (getRetrievalSemantics() == STANDARD) {
            return ifNotExists(getConstraintFactory().forEachIncludingUnassigned(otherClass), joiners);
        } else {
            return ifNotExists(getConstraintFactory().fromUnfiltered(otherClass), joiners);
        }
    }

    @Override
    default @NonNull UniConstraintStream<A> distinct() {
        if (guaranteesDistinct()) {
            return this;
        } else {
            return groupBy(identity());
        }
    }

    @Override
    @NonNull
    default UniConstraintStub<A> penalizeWeighted(@NonNull ToIntFunction<A> matchWeigher) {
        return innerImpact(matchWeigher, ScoreImpactType.PENALTY);
    }

    UniConstraintStub<A> innerImpact(ToIntFunction<A> matchWeigher, ScoreImpactType scoreImpactType);

    @Override
    @NonNull
    default UniConstraintStub<A> penalizeWeightedLong(@NonNull ToLongFunction<A> matchWeigher) {
        return innerImpactLong(matchWeigher, ScoreImpactType.PENALTY);
    }

    UniConstraintStub<A> innerImpactLong(ToLongFunction<A> matchWeigher, ScoreImpactType scoreImpactType);

    @Override
    @NonNull
    default UniConstraintStub<A> penalizeWeightedBigDecimal(@NonNull Function<A, BigDecimal> matchWeigher) {
        return innerImpactBigDecimal(matchWeigher, ScoreImpactType.PENALTY);
    }

    UniConstraintStub<A> innerImpactBigDecimal(Function<A, BigDecimal> matchWeigher, ScoreImpactType scoreImpactType);

    @Override
    @NonNull
    default UniConstraintStub<A> rewardWeighted(@NonNull ToIntFunction<A> matchWeigher) {
        return innerImpact(matchWeigher, ScoreImpactType.REWARD);
    }

    @Override
    @NonNull
    default UniConstraintStub<A> rewardWeightedLong(@NonNull ToLongFunction<A> matchWeigher) {
        return innerImpactLong(matchWeigher, ScoreImpactType.REWARD);
    }

    @Override
    @NonNull
    default UniConstraintStub<A> rewardWeightedBigDecimal(@NonNull Function<A, BigDecimal> matchWeigher) {
        return innerImpactBigDecimal(matchWeigher, ScoreImpactType.REWARD);
    }

    @Override
    @NonNull
    default UniConstraintStub<A> impactWeighted(@NonNull ToIntFunction<A> matchWeigher) {
        return innerImpact(matchWeigher, ScoreImpactType.MIXED);
    }

    @Override
    @NonNull
    default UniConstraintStub<A> impactWeightedLong(@NonNull ToLongFunction<A> matchWeigher) {
        return innerImpactLong(matchWeigher, ScoreImpactType.MIXED);
    }

    @Override
    @NonNull
    default UniConstraintStub<A> impactWeightedBigDecimal(@NonNull Function<A, BigDecimal> matchWeigher) {
        return innerImpactBigDecimal(matchWeigher, ScoreImpactType.MIXED);
    }

}
