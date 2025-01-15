package ai.timefold.solver.core.impl.score.stream.bavet.tri;

import java.util.function.BiFunction;

import ai.timefold.solver.core.impl.score.stream.bavet.common.AbstractConcatNode;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.BiTuple;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.TriTuple;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.TupleLifecycle;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.UniversalTuple;

final class ConcatTriBiNode<A, B, C>
        extends AbstractConcatNode<TriTuple<A, B, C>, BiTuple<A, B>, TriTuple<A, B, C>> {

    private final BiFunction<A, B, C> paddingFunction;

    ConcatTriBiNode(BiFunction<A, B, C> paddingFunction,
            TupleLifecycle<TriTuple<A, B, C>> nextNodesTupleLifecycle,
            int inputStoreIndexLeftOutTupleList, int inputStoreIndexRightOutTupleList,
            int outputStoreSize) {
        super(nextNodesTupleLifecycle, inputStoreIndexLeftOutTupleList, inputStoreIndexRightOutTupleList, outputStoreSize);
        this.paddingFunction = paddingFunction;
    }

    @Override
    protected TriTuple<A, B, C> getOutTupleFromLeft(TriTuple<A, B, C> leftTuple) {
        return new UniversalTuple<>(leftTuple.getA(), leftTuple.getB(), leftTuple.getC(), outputStoreSize);
    }

    @Override
    protected TriTuple<A, B, C> getOutTupleFromRight(BiTuple<A, B> rightTuple) {
        var factA = rightTuple.getA();
        var factB = rightTuple.getB();
        return new UniversalTuple<>(factA, factB, paddingFunction.apply(factA, factB), outputStoreSize);
    }

    @Override
    protected void updateOutTupleFromLeft(TriTuple<A, B, C> leftTuple, TriTuple<A, B, C> outTuple) {
        outTuple.setA(leftTuple.getA());
        outTuple.setB(leftTuple.getB());
        outTuple.setC(leftTuple.getC());
    }

    @Override
    protected void updateOutTupleFromRight(BiTuple<A, B> rightTuple, TriTuple<A, B, C> outTuple) {
        outTuple.setA(rightTuple.getA());
        outTuple.setB(rightTuple.getB());
    }

}
