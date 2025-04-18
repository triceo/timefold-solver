package ai.timefold.solver.core.impl.score.stream.bavet.tri;

import java.util.Objects;

import ai.timefold.solver.core.api.function.TriFunction;
import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.impl.bavet.tri.MapTriToBiNode;
import ai.timefold.solver.core.impl.score.stream.bavet.BavetConstraintFactory;
import ai.timefold.solver.core.impl.score.stream.bavet.common.ConstraintNodeBuildHelper;
import ai.timefold.solver.core.impl.score.stream.bavet.common.bridge.BavetAftBridgeBiConstraintStream;

final class BavetBiMapTriConstraintStream<Solution_, A, B, C, NewA, NewB>
        extends BavetAbstractTriConstraintStream<Solution_, A, B, C> {

    private final TriFunction<A, B, C, NewA> mappingFunctionA;
    private final TriFunction<A, B, C, NewB> mappingFunctionB;
    private BavetAftBridgeBiConstraintStream<Solution_, NewA, NewB> aftStream;

    public BavetBiMapTriConstraintStream(BavetConstraintFactory<Solution_> constraintFactory,
            BavetAbstractTriConstraintStream<Solution_, A, B, C> parent, TriFunction<A, B, C, NewA> mappingFunctionA,
            TriFunction<A, B, C, NewB> mappingFunctionB) {
        super(constraintFactory, parent);
        this.mappingFunctionA = mappingFunctionA;
        this.mappingFunctionB = mappingFunctionB;
    }

    public void setAftBridge(BavetAftBridgeBiConstraintStream<Solution_, NewA, NewB> aftStream) {
        this.aftStream = aftStream;
    }

    // ************************************************************************
    // Node creation
    // ************************************************************************

    @Override
    public boolean guaranteesDistinct() {
        return false;
    }

    @Override
    public <Score_ extends Score<Score_>> void buildNode(ConstraintNodeBuildHelper<Solution_, Score_> buildHelper) {
        assertEmptyChildStreamList();
        int inputStoreIndex = buildHelper.reserveTupleStoreIndex(parent.getTupleSource());
        int outputStoreSize = buildHelper.extractTupleStoreSize(aftStream);
        var node = new MapTriToBiNode<>(inputStoreIndex, mappingFunctionA, mappingFunctionB,
                buildHelper.getAggregatedTupleLifecycle(aftStream.getChildStreamList()), outputStoreSize);
        buildHelper.addNode(node, this);
    }

    // ************************************************************************
    // Equality for node sharing
    // ************************************************************************

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || getClass() != object.getClass())
            return false;
        BavetBiMapTriConstraintStream<?, ?, ?, ?, ?, ?> that = (BavetBiMapTriConstraintStream<?, ?, ?, ?, ?, ?>) object;
        return Objects.equals(parent, that.parent) && Objects.equals(mappingFunctionA, that.mappingFunctionA) && Objects.equals(
                mappingFunctionB,
                that.mappingFunctionB);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, mappingFunctionA, mappingFunctionB);
    }

    // ************************************************************************
    // Getters/setters
    // ************************************************************************

    @Override
    public String toString() {
        return "BiMap()";
    }

}
