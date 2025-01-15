package ai.timefold.solver.core.impl.score.stream.bavet.quad;

import ai.timefold.solver.core.api.function.TriFunction;
import ai.timefold.solver.core.impl.score.stream.bavet.common.AbstractConcatNode;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.QuadTuple;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.TriTuple;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.TupleLifecycle;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.UniversalTuple;

final class ConcatTriQuadNode<A, B, C, D>
        extends AbstractConcatNode<TriTuple<A, B, C>, QuadTuple<A, B, C, D>, QuadTuple<A, B, C, D>> {

    private final TriFunction<A, B, C, D> paddingFunction;

    ConcatTriQuadNode(TriFunction<A, B, C, D> paddingFunction, TupleLifecycle<QuadTuple<A, B, C, D>> nextNodesTupleLifecycle,
            int inputStoreIndexLeftOutTupleList, int inputStoreIndexRightOutTupleList,
            int outputStoreSize) {
        super(nextNodesTupleLifecycle, inputStoreIndexLeftOutTupleList, inputStoreIndexRightOutTupleList, outputStoreSize);
        this.paddingFunction = paddingFunction;
    }

    @Override
    protected QuadTuple<A, B, C, D> getOutTupleFromLeft(TriTuple<A, B, C> leftTuple) {
        var factA = leftTuple.getA();
        var factB = leftTuple.getB();
        var factC = leftTuple.getC();
        return new UniversalTuple<>(factA, factB, factC,
                paddingFunction.apply(factA, factB, factC),
                outputStoreSize);
    }

    @Override
    protected QuadTuple<A, B, C, D> getOutTupleFromRight(QuadTuple<A, B, C, D> rightTuple) {
        return new UniversalTuple<>(rightTuple.getA(), rightTuple.getB(), rightTuple.getC(), rightTuple.getD(),
                outputStoreSize);
    }

    @Override
    protected void updateOutTupleFromLeft(TriTuple<A, B, C> leftTuple, QuadTuple<A, B, C, D> outTuple) {
        outTuple.setA(leftTuple.getA());
        outTuple.setB(leftTuple.getB());
        outTuple.setC(leftTuple.getC());
    }

    @Override
    protected void updateOutTupleFromRight(QuadTuple<A, B, C, D> rightTuple, QuadTuple<A, B, C, D> outTuple) {
        outTuple.setA(rightTuple.getA());
        outTuple.setB(rightTuple.getB());
        outTuple.setC(rightTuple.getC());
        outTuple.setD(rightTuple.getD());
    }

}
