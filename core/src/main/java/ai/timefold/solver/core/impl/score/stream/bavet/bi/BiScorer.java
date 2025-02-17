package ai.timefold.solver.core.impl.score.stream.bavet.bi;

import ai.timefold.solver.core.impl.bavet.common.AbstractScorer;
import ai.timefold.solver.core.impl.bavet.common.tuple.BiTuple;
import ai.timefold.solver.core.impl.score.stream.common.bi.BiImpactFunction;
import ai.timefold.solver.core.impl.score.stream.common.inliner.UndoScoreImpacter;
import ai.timefold.solver.core.impl.score.stream.common.inliner.WeightedScoreImpacter;

final class BiScorer<A, B> extends AbstractScorer<BiTuple<A, B>> {

    private final BiImpactFunction<A, B> impactFunction;

    public BiScorer(WeightedScoreImpacter<?, ?> weightedScoreImpacter, BiImpactFunction<A, B> impactFunction,
            int inputStoreIndex) {
        super(weightedScoreImpacter, inputStoreIndex);
        this.impactFunction = impactFunction;
    }

    @Override
    protected UndoScoreImpacter impact(BiTuple<A, B> tuple) {
        try {
            return impactFunction.apply(weightedScoreImpacter, tuple.factA, tuple.factB);
        } catch (Exception e) {
            throw createExceptionOnImpact(tuple, e);
        }
    }
}
