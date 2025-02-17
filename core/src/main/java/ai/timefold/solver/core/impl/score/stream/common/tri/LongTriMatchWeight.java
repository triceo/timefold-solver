package ai.timefold.solver.core.impl.score.stream.common.tri;

import java.util.Objects;

import ai.timefold.solver.core.api.function.ToLongTriFunction;
import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.impl.score.stream.common.MatchWeight;
import ai.timefold.solver.core.impl.score.stream.common.inliner.ConstraintMatchSupplier;
import ai.timefold.solver.core.impl.score.stream.common.inliner.UndoScoreImpacter;
import ai.timefold.solver.core.impl.score.stream.common.inliner.WeightedScoreImpacter;

final class LongTriMatchWeight<A, B, C> extends AbstractTriMatchWeight<A, B, C> {

    private final ToLongTriFunction<A, B, C> weigher;

    LongTriMatchWeight(ToLongTriFunction<A, B, C> weigher) {
        this.weigher = Objects.requireNonNull(weigher);
    }

    @Override
    protected UndoScoreImpacter naked(WeightedScoreImpacter<?, ?> impacter, A a, B b, C c) {
        var matchWeight = weigher.applyAsLong(a, b, c);
        return impacter.impactScore(matchWeight, null);
    }

    @Override
    protected UndoScoreImpacter partial(WeightedScoreImpacter<?, ?> impacter, A a, B b, C c) {
        var matchWeight = weigher.applyAsLong(a, b, c);
        return MatchWeight.impactWithConstraintMatchNoJustifications(impacter, matchWeight);
    }

    @Override
    protected UndoScoreImpacter full(WeightedScoreImpacter<?, ?> impacter, A a, B b, C c) {
        var matchWeight = weigher.applyAsLong(a, b, c);
        return impactWithConstraintMatch(impacter, matchWeight, a, b, c);
    }

    private static <A, B, C, Score_ extends Score<Score_>> UndoScoreImpacter
            impactWithConstraintMatch(WeightedScoreImpacter<Score_, ?> impacter, long matchWeight, A a, B b, C c) {
        var constraint = impacter.getContext().getConstraint();
        var constraintMatchSupplier = ConstraintMatchSupplier.<A, B, C, Score_> of(constraint.getJustificationMapping(),
                constraint.getIndictedObjectsMapping(), a, b, c);
        return impacter.impactScore(matchWeight, constraintMatchSupplier);
    }

}
