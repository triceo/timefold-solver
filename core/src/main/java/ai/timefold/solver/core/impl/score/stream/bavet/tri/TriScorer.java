package ai.timefold.solver.core.impl.score.stream.bavet.tri;

import ai.timefold.solver.core.impl.bavet.common.AbstractScorer;
import ai.timefold.solver.core.impl.bavet.common.tuple.TriTuple;
import ai.timefold.solver.core.impl.score.stream.common.inliner.UndoScoreImpacter;
import ai.timefold.solver.core.impl.score.stream.common.inliner.WeightedScoreImpacter;
import ai.timefold.solver.core.impl.score.stream.common.tri.TriImpactFunction;

final class TriScorer<A, B, C> extends AbstractScorer<TriTuple<A, B, C>> {

    private final TriImpactFunction<A, B, C> impactFunction;

    public TriScorer(WeightedScoreImpacter<?, ?> weightedScoreImpacter, TriImpactFunction<A, B, C> impactFunction,
            int inputStoreIndex) {
        super(weightedScoreImpacter, inputStoreIndex);
        this.impactFunction = impactFunction;
    }

    @Override
    protected UndoScoreImpacter impact(TriTuple<A, B, C> tuple) {
        try {
            return impactFunction.apply(weightedScoreImpacter, tuple.factA, tuple.factB, tuple.factC);
        } catch (Exception e) {
            throw createExceptionOnImpact(tuple, e);
        }
    }
}
