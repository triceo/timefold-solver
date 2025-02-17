package ai.timefold.solver.core.impl.score.stream.common.uni;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Function;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.impl.score.stream.common.MatchWeight;
import ai.timefold.solver.core.impl.score.stream.common.inliner.ConstraintMatchSupplier;
import ai.timefold.solver.core.impl.score.stream.common.inliner.UndoScoreImpacter;
import ai.timefold.solver.core.impl.score.stream.common.inliner.WeightedScoreImpacter;

final class BigDecimalUniMatchWeight<A> extends AbstractUniMatchWeight<A> {

    private final Function<A, BigDecimal> weigher;

    BigDecimalUniMatchWeight(Function<A, BigDecimal> weigher) {
        this.weigher = Objects.requireNonNull(weigher);
    }

    @Override
    protected UndoScoreImpacter naked(WeightedScoreImpacter<?, ?> impacter, A a) {
        var matchWeight = weigher.apply(a);
        return impacter.impactScore(matchWeight, null);
    }

    @Override
    protected UndoScoreImpacter partial(WeightedScoreImpacter<?, ?> impacter, A a) {
        var matchWeight = weigher.apply(a);
        return MatchWeight.impactWithConstraintMatchNoJustifications(impacter, matchWeight);
    }

    @Override
    protected UndoScoreImpacter full(WeightedScoreImpacter<?, ?> impacter, A a) {
        var matchWeight = weigher.apply(a);
        return impactWithConstraintMatch(impacter, matchWeight, a);
    }

    private static <A, Score_ extends Score<Score_>> UndoScoreImpacter
            impactWithConstraintMatch(WeightedScoreImpacter<Score_, ?> impacter, BigDecimal matchWeight, A a) {
        var constraint = impacter.getContext().getConstraint();
        var constraintMatchSupplier = ConstraintMatchSupplier.<A, Score_> of(constraint.getJustificationMapping(),
                constraint.getIndictedObjectsMapping(), a);
        return impacter.impactScore(matchWeight, constraintMatchSupplier);
    }

}
