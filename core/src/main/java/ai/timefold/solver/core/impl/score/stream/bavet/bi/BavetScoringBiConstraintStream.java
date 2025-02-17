package ai.timefold.solver.core.impl.score.stream.bavet.bi;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.impl.bavet.common.BavetScoringConstraintStream;
import ai.timefold.solver.core.impl.bavet.common.NodeBuildHelper;
import ai.timefold.solver.core.impl.score.constraint.ConstraintMatchPolicy;
import ai.timefold.solver.core.impl.score.stream.bavet.BavetConstraint;
import ai.timefold.solver.core.impl.score.stream.bavet.BavetConstraintFactory;
import ai.timefold.solver.core.impl.score.stream.common.bi.AbstractBiMatchWeight;
import ai.timefold.solver.core.impl.score.stream.common.bi.BiImpactFunction;

final class BavetScoringBiConstraintStream<Solution_, A, B>
        extends BavetAbstractBiConstraintStream<Solution_, A, B>
        implements BavetScoringConstraintStream<Solution_> {

    private BavetConstraint<Solution_> constraint;

    public BavetScoringBiConstraintStream(BavetConstraintFactory<Solution_> constraintFactory,
            BavetAbstractBiConstraintStream<Solution_, A, B> parent) {
        super(constraintFactory, parent);
    }

    @Override
    public void setConstraint(BavetConstraint<Solution_> constraint) {
        this.constraint = constraint;
    }

    // ************************************************************************
    // Node creation
    // ************************************************************************

    @Override
    public <Score_ extends Score<Score_>> void buildNode(NodeBuildHelper<Score_> buildHelper) {
        assertEmptyChildStreamList();
        var impactFunction = buildImpactFunction(buildHelper.getScoreInliner().getConstraintMatchPolicy());
        var weightedScoreImpacter = buildHelper.getScoreInliner().buildWeightedScoreImpacter(constraint);
        var scorer = new BiScorer<>(weightedScoreImpacter, impactFunction,
                buildHelper.reserveTupleStoreIndex(parent.getTupleSource()));
        buildHelper.putInsertUpdateRetract(this, scorer);
    }

    private BiImpactFunction<A, B> buildImpactFunction(ConstraintMatchPolicy constraintMatchPolicy) {
        AbstractBiMatchWeight<A, B> matchWeight = constraint.getMatchWeight();
        return matchWeight.createImpactFunction(constraintMatchPolicy);
    }

    @Override
    public String toString() {
        return "Scoring(" + constraint.getConstraintRef() + ")";
    }

}
