package ai.timefold.solver.core.impl.score.stream.bavet.tri;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.impl.bavet.common.BavetScoringConstraintStream;
import ai.timefold.solver.core.impl.bavet.common.NodeBuildHelper;
import ai.timefold.solver.core.impl.score.constraint.ConstraintMatchPolicy;
import ai.timefold.solver.core.impl.score.stream.bavet.BavetConstraint;
import ai.timefold.solver.core.impl.score.stream.bavet.BavetConstraintFactory;
import ai.timefold.solver.core.impl.score.stream.common.tri.AbstractTriMatchWeight;
import ai.timefold.solver.core.impl.score.stream.common.tri.TriImpactFunction;

final class BavetScoringTriConstraintStream<Solution_, A, B, C>
        extends BavetAbstractTriConstraintStream<Solution_, A, B, C>
        implements BavetScoringConstraintStream<Solution_> {

    private BavetConstraint<Solution_> constraint;

    public BavetScoringTriConstraintStream(BavetConstraintFactory<Solution_> constraintFactory,
            BavetAbstractTriConstraintStream<Solution_, A, B, C> parent) {
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
        var scorer = new TriScorer<>(weightedScoreImpacter, impactFunction,
                buildHelper.reserveTupleStoreIndex(parent.getTupleSource()));
        buildHelper.putInsertUpdateRetract(this, scorer);
    }

    private TriImpactFunction<A, B, C> buildImpactFunction(ConstraintMatchPolicy constraintMatchPolicy) {
        AbstractTriMatchWeight<A, B, C> matchWeight = constraint.getMatchWeight();
        return matchWeight.createImpactFunction(constraintMatchPolicy);
    }

    @Override
    public String toString() {
        return "Scoring(" + constraint.getConstraintRef() + ")";
    }

}
