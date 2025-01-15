package ai.timefold.solver.core.impl.score.stream.bavet.quad;

import java.util.function.BiFunction;

import ai.timefold.solver.core.impl.score.stream.bavet.common.AbstractConcatNode;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.BiTuple;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.QuadTuple;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.TupleLifecycle;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.UniversalTuple;

final class ConcatQuadBiNode<A, B, C, D>
        extends AbstractConcatNode<QuadTuple<A, B, C, D>, BiTuple<A, B>, QuadTuple<A, B, C, D>> {

    private final BiFunction<A, B, C> paddingFunctionC;
    private final BiFunction<A, B, D> paddingFunctionD;

    ConcatQuadBiNode(BiFunction<A, B, C> paddingFunctionC, BiFunction<A, B, D> paddingFunctionD,
            TupleLifecycle<QuadTuple<A, B, C, D>> nextNodesTupleLifecycle,
            int inputStoreIndexLeftOutTupleList, int inputStoreIndexRightOutTupleList,
            int outputStoreSize) {
        super(nextNodesTupleLifecycle, inputStoreIndexLeftOutTupleList, inputStoreIndexRightOutTupleList, outputStoreSize);
        this.paddingFunctionC = paddingFunctionC;
        this.paddingFunctionD = paddingFunctionD;
    }

    @Override
    protected QuadTuple<A, B, C, D> getOutTupleFromLeft(QuadTuple<A, B, C, D> leftTuple) {
        return new UniversalTuple<>(leftTuple.getA(), leftTuple.getB(), leftTuple.getC(), leftTuple.getD(), outputStoreSize);
    }

    @Override
    protected QuadTuple<A, B, C, D> getOutTupleFromRight(BiTuple<A, B> rightTuple) {
        var factA = rightTuple.getA();
        var factB = rightTuple.getB();
        return new UniversalTuple<>(factA, factB,
                paddingFunctionC.apply(factA, factB), paddingFunctionD.apply(factA, factB),
                outputStoreSize);
    }

    @Override
    protected void updateOutTupleFromLeft(QuadTuple<A, B, C, D> leftTuple, QuadTuple<A, B, C, D> outTuple) {
        outTuple.setA(leftTuple.getA());
        outTuple.setB(leftTuple.getB());
        outTuple.setC(leftTuple.getC());
        outTuple.setD(leftTuple.getD());
    }

    @Override
    protected void updateOutTupleFromRight(BiTuple<A, B> rightTuple, QuadTuple<A, B, C, D> outTuple) {
        outTuple.setA(rightTuple.getA());
        outTuple.setB(rightTuple.getB());
    }

}
