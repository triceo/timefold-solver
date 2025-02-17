package ai.timefold.solver.core.impl.score.stream.bavet.quad;

import ai.timefold.solver.core.impl.bavet.common.AbstractScorer;
import ai.timefold.solver.core.impl.bavet.common.tuple.QuadTuple;
import ai.timefold.solver.core.impl.score.stream.common.inliner.UndoScoreImpacter;
import ai.timefold.solver.core.impl.score.stream.common.inliner.WeightedScoreImpacter;
import ai.timefold.solver.core.impl.score.stream.common.quad.QuadImpactFunction;

final class QuadScorer<A, B, C, D> extends AbstractScorer<QuadTuple<A, B, C, D>> {

    private final QuadImpactFunction<A, B, C, D> impactFunction;

    public QuadScorer(WeightedScoreImpacter<?, ?> weightedScoreImpacter, QuadImpactFunction<A, B, C, D> impactFunction,
            int inputStoreIndex) {
        super(weightedScoreImpacter, inputStoreIndex);
        this.impactFunction = impactFunction;
    }

    @Override
    protected UndoScoreImpacter impact(QuadTuple<A, B, C, D> tuple) {
        try {
            return impactFunction.apply(weightedScoreImpacter, tuple.factA, tuple.factB, tuple.factC, tuple.factD);
        } catch (Exception e) {
            throw createExceptionOnImpact(tuple, e);
        }
    }
}
