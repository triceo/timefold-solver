package ai.timefold.solver.core.impl.score.stream.bavet.uni;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.impl.bavet.common.BavetScoringConstraintStream;
import ai.timefold.solver.core.impl.bavet.common.NodeBuildHelper;
import ai.timefold.solver.core.impl.score.constraint.ConstraintMatchPolicy;
import ai.timefold.solver.core.impl.score.stream.bavet.BavetConstraint;
import ai.timefold.solver.core.impl.score.stream.bavet.BavetConstraintFactory;
import ai.timefold.solver.core.impl.score.stream.common.uni.AbstractUniMatchWeight;
import ai.timefold.solver.core.impl.score.stream.common.uni.UniImpactFunction;

final class BavetScoringUniConstraintStream<Solution_, A>
        extends BavetAbstractUniConstraintStream<Solution_, A>
        implements BavetScoringConstraintStream<Solution_> {

    private BavetConstraint<Solution_> constraint;

    public BavetScoringUniConstraintStream(BavetConstraintFactory<Solution_> constraintFactory,
            BavetAbstractUniConstraintStream<Solution_, A> parent) {
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
        var scorer = new UniScorer<>(weightedScoreImpacter, impactFunction,
                buildHelper.reserveTupleStoreIndex(parent.getTupleSource()));
        buildHelper.putInsertUpdateRetract(this, scorer);
    }

    private UniImpactFunction<A> buildImpactFunction(ConstraintMatchPolicy constraintMatchPolicy) {
        AbstractUniMatchWeight<A> matchWeight = constraint.getMatchWeight();
        return matchWeight.createImpactFunction(constraintMatchPolicy);
    }

    @Override
    public String toString() {
        return "Scoring(" + constraint.getConstraintRef() + ")";
    }

}
