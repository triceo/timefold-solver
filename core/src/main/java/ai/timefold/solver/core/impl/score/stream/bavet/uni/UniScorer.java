package ai.timefold.solver.core.impl.score.stream.bavet.uni;

import ai.timefold.solver.core.impl.bavet.common.AbstractScorer;
import ai.timefold.solver.core.impl.bavet.common.tuple.UniTuple;
import ai.timefold.solver.core.impl.score.stream.common.inliner.UndoScoreImpacter;
import ai.timefold.solver.core.impl.score.stream.common.inliner.WeightedScoreImpacter;
import ai.timefold.solver.core.impl.score.stream.common.uni.UniImpactFunction;

final class UniScorer<A> extends AbstractScorer<UniTuple<A>> {

    private final UniImpactFunction<A> impactFunction;

    public UniScorer(WeightedScoreImpacter<?, ?> weightedScoreImpacter, UniImpactFunction<A> impactFunction,
            int inputStoreIndex) {
        super(weightedScoreImpacter, inputStoreIndex);
        this.impactFunction = impactFunction;
    }

    @Override
    protected UndoScoreImpacter impact(UniTuple<A> tuple) {
        try {
            return impactFunction.apply(weightedScoreImpacter, tuple.factA);
        } catch (Exception e) {
            throw createExceptionOnImpact(tuple, e);
        }
    }
}
