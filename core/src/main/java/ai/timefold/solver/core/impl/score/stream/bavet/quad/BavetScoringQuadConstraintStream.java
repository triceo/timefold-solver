package ai.timefold.solver.core.impl.score.stream.bavet.quad;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.impl.bavet.common.BavetScoringConstraintStream;
import ai.timefold.solver.core.impl.bavet.common.NodeBuildHelper;
import ai.timefold.solver.core.impl.score.constraint.ConstraintMatchPolicy;
import ai.timefold.solver.core.impl.score.stream.bavet.BavetConstraint;
import ai.timefold.solver.core.impl.score.stream.bavet.BavetConstraintFactory;
import ai.timefold.solver.core.impl.score.stream.common.quad.QuadImpactFunction;
import ai.timefold.solver.core.impl.score.stream.common.quad.QuadMatchWeight;

final class BavetScoringQuadConstraintStream<Solution_, A, B, C, D>
        extends BavetAbstractQuadConstraintStream<Solution_, A, B, C, D>
        implements BavetScoringConstraintStream<Solution_> {

    private BavetConstraint<Solution_> constraint;

    public BavetScoringQuadConstraintStream(BavetConstraintFactory<Solution_> constraintFactory,
            BavetAbstractQuadConstraintStream<Solution_, A, B, C, D> parent) {
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
        var scorer = new QuadScorer<>(weightedScoreImpacter, impactFunction,
                buildHelper.reserveTupleStoreIndex(parent.getTupleSource()));
        buildHelper.putInsertUpdateRetract(this, scorer);
    }

    private QuadImpactFunction<A, B, C, D> buildImpactFunction(ConstraintMatchPolicy constraintMatchPolicy) {
        QuadMatchWeight<A, B, C, D> matchWeight = constraint.getMatchWeight();
        return matchWeight.createImpactFunction(constraintMatchPolicy);
    }

    @Override
    public String toString() {
        return "Scoring(" + constraint.getConstraintRef() + ")";
    }

}
