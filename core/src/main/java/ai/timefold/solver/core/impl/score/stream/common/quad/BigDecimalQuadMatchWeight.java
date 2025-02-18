package ai.timefold.solver.core.impl.score.stream.common.quad;

import java.math.BigDecimal;
import java.util.Objects;

import ai.timefold.solver.core.api.function.QuadFunction;
import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.impl.score.stream.common.MatchWeight;
import ai.timefold.solver.core.impl.score.stream.common.inliner.ConstraintMatchSupplier;
import ai.timefold.solver.core.impl.score.stream.common.inliner.UndoScoreImpacter;
import ai.timefold.solver.core.impl.score.stream.common.inliner.WeightedScoreImpacter;

final class BigDecimalQuadMatchWeight<A, B, C, D> extends AbstractQuadMatchWeight<A, B, C, D> {

    private final QuadFunction<A, B, C, D, BigDecimal> weigher;

    public BigDecimalQuadMatchWeight(QuadFunction<A, B, C, D, BigDecimal> weigher) {
        this.weigher = Objects.requireNonNull(weigher);
    }

    @Override
    protected UndoScoreImpacter naked(WeightedScoreImpacter<?, ?> impacter, A a, B b, C c, D d) {
        var matchWeight = weigher.apply(a, b, c, d);
        return impacter.impactScore(matchWeight, null);
    }

    @Override
    protected UndoScoreImpacter partial(WeightedScoreImpacter<?, ?> impacter, A a, B b, C c, D d) {
        var matchWeight = weigher.apply(a, b, c, d);
        return MatchWeight.impactWithConstraintMatchNoJustifications(impacter, matchWeight);
    }

    @Override
    protected UndoScoreImpacter full(WeightedScoreImpacter<?, ?> impacter, A a, B b, C c, D d) {
        var matchWeight = weigher.apply(a, b, c, d);
        return impactWithConstraintMatch(impacter, matchWeight, a, b, c, d);
    }

    private static <A, B, C, D, Score_ extends Score<Score_>> UndoScoreImpacter
            impactWithConstraintMatch(WeightedScoreImpacter<Score_, ?> impacter, BigDecimal matchWeight, A a, B b, C c, D d) {
        var constraint = impacter.getContext().getConstraint();
        var constraintMatchSupplier = ConstraintMatchSupplier.<A, B, C, D, Score_> of(constraint.getJustificationMapping(),
                constraint.getIndictedObjectsMapping(), a, b, c, d);
        return impacter.impactScore(matchWeight, constraintMatchSupplier);
    }

}
