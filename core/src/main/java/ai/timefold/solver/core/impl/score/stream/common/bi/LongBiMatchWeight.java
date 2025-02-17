package ai.timefold.solver.core.impl.score.stream.common.bi;

import java.util.Objects;
import java.util.function.ToLongBiFunction;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.impl.score.stream.common.MatchWeight;
import ai.timefold.solver.core.impl.score.stream.common.inliner.ConstraintMatchSupplier;
import ai.timefold.solver.core.impl.score.stream.common.inliner.UndoScoreImpacter;
import ai.timefold.solver.core.impl.score.stream.common.inliner.WeightedScoreImpacter;

final class LongBiMatchWeight<A, B> extends AbstractBiMatchWeight<A, B> {

    private final ToLongBiFunction<A, B> weigher;

    LongBiMatchWeight(ToLongBiFunction<A, B> weigher) {
        this.weigher = Objects.requireNonNull(weigher);
    }

    @Override
    protected UndoScoreImpacter naked(WeightedScoreImpacter<?, ?> impacter, A a, B b) {
        var matchWeight = weigher.applyAsLong(a, b);
        return impacter.impactScore(matchWeight, null);
    }

    @Override
    protected UndoScoreImpacter partial(WeightedScoreImpacter<?, ?> impacter, A a, B b) {
        var matchWeight = weigher.applyAsLong(a, b);
        return MatchWeight.impactWithConstraintMatchNoJustifications(impacter, matchWeight);
    }

    @Override
    protected UndoScoreImpacter full(WeightedScoreImpacter<?, ?> impacter, A a, B b) {
        var matchWeight = weigher.applyAsLong(a, b);
        return impactWithConstraintMatch(impacter, matchWeight, a, b);
    }

    private static <A, B, Score_ extends Score<Score_>> UndoScoreImpacter
            impactWithConstraintMatch(WeightedScoreImpacter<Score_, ?> impacter, long matchWeight, A a, B b) {
        var constraint = impacter.getContext().getConstraint();
        var constraintMatchSupplier = ConstraintMatchSupplier.<A, B, Score_> of(constraint.getJustificationMapping(),
                constraint.getIndictedObjectsMapping(), a, b);
        return impacter.impactScore(matchWeight, constraintMatchSupplier);
    }

}
