package ai.timefold.solver.core.impl.score.stream.bavet.bi;

import java.util.function.Function;

import ai.timefold.solver.core.impl.score.stream.bavet.common.AbstractConcatNode;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.BiTuple;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.TupleLifecycle;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.UniTuple;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.UniversalTuple;

final class ConcatUniBiNode<A, B>
        extends AbstractConcatNode<UniTuple<A>, BiTuple<A, B>, BiTuple<A, B>> {

    private final Function<A, B> paddingFunction;

    ConcatUniBiNode(Function<A, B> paddingFunction, TupleLifecycle<BiTuple<A, B>> nextNodesTupleLifecycle,
            int inputStoreIndexLeftOutTupleList, int inputStoreIndexRightOutTupleList,
            int outputStoreSize) {
        super(nextNodesTupleLifecycle, inputStoreIndexLeftOutTupleList, inputStoreIndexRightOutTupleList,
                outputStoreSize);
        this.paddingFunction = paddingFunction;
    }

    @Override
    protected BiTuple<A, B> getOutTupleFromLeft(UniTuple<A> leftTuple) {
        var factA = leftTuple.getA();
        return new UniversalTuple<>(factA, paddingFunction.apply(factA), outputStoreSize);
    }

    @Override
    protected BiTuple<A, B> getOutTupleFromRight(BiTuple<A, B> rightTuple) {
        return new UniversalTuple<>(rightTuple.getA(), rightTuple.getB(), outputStoreSize);
    }

    @Override
    protected void updateOutTupleFromLeft(UniTuple<A> leftTuple, BiTuple<A, B> outTuple) {
        outTuple.setA(leftTuple.getA());
    }

    @Override
    protected void updateOutTupleFromRight(BiTuple<A, B> rightTuple, BiTuple<A, B> outTuple) {
        outTuple.setA(rightTuple.getA());
        outTuple.setB(rightTuple.getB());
    }

}
