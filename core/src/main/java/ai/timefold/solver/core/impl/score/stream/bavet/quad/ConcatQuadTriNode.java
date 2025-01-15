package ai.timefold.solver.core.impl.score.stream.bavet.quad;

import ai.timefold.solver.core.api.function.TriFunction;
import ai.timefold.solver.core.impl.score.stream.bavet.common.AbstractConcatNode;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.QuadTuple;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.TriTuple;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.TupleLifecycle;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.UniversalTuple;

final class ConcatQuadTriNode<A, B, C, D>
        extends AbstractConcatNode<QuadTuple<A, B, C, D>, TriTuple<A, B, C>, QuadTuple<A, B, C, D>> {

    private final TriFunction<A, B, C, D> paddingFunction;

    ConcatQuadTriNode(TriFunction<A, B, C, D> paddingFunction, TupleLifecycle<QuadTuple<A, B, C, D>> nextNodesTupleLifecycle,
            int inputStoreIndexLeftOutTupleList, int inputStoreIndexRightOutTupleList,
            int outputStoreSize) {
        super(nextNodesTupleLifecycle, inputStoreIndexLeftOutTupleList, inputStoreIndexRightOutTupleList, outputStoreSize);
        this.paddingFunction = paddingFunction;
    }

    @Override
    protected QuadTuple<A, B, C, D> getOutTupleFromLeft(QuadTuple<A, B, C, D> leftTuple) {
        return new UniversalTuple<>(leftTuple.getA(), leftTuple.getB(), leftTuple.getC(), leftTuple.getD(), outputStoreSize);
    }

    @Override
    protected QuadTuple<A, B, C, D> getOutTupleFromRight(TriTuple<A, B, C> rightTuple) {
        var factA = rightTuple.getA();
        var factB = rightTuple.getB();
        var factC = rightTuple.getC();
        return new UniversalTuple<>(factA, factB, factC,
                paddingFunction.apply(factA, factB, factC),
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
    protected void updateOutTupleFromRight(TriTuple<A, B, C> rightTuple, QuadTuple<A, B, C, D> outTuple) {
        outTuple.setA(rightTuple.getA());
        outTuple.setB(rightTuple.getB());
        outTuple.setC(rightTuple.getC());
    }

}
