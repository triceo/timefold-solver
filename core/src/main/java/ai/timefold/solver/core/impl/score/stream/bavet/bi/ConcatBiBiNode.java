package ai.timefold.solver.core.impl.score.stream.bavet.bi;

import ai.timefold.solver.core.impl.score.stream.bavet.common.AbstractConcatNode;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.BiTuple;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.TupleLifecycle;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.UniversalTuple;

final class ConcatBiBiNode<A, B>
        extends AbstractConcatNode<BiTuple<A, B>, BiTuple<A, B>, BiTuple<A, B>> {

    ConcatBiBiNode(TupleLifecycle<BiTuple<A, B>> nextNodesTupleLifecycle,
            int inputStoreIndexLeftOutTupleList, int inputStoreIndexRightOutTupleList,
            int outputStoreSize) {
        super(nextNodesTupleLifecycle, inputStoreIndexLeftOutTupleList, inputStoreIndexRightOutTupleList, outputStoreSize);
    }

    @Override
    protected BiTuple<A, B> getOutTupleFromLeft(BiTuple<A, B> leftTuple) {
        return new UniversalTuple<>(leftTuple.getA(), leftTuple.getB(), outputStoreSize);
    }

    @Override
    protected BiTuple<A, B> getOutTupleFromRight(BiTuple<A, B> rightTuple) {
        return new UniversalTuple<>(rightTuple.getA(), rightTuple.getB(), outputStoreSize);
    }

    @Override
    protected void updateOutTupleFromLeft(BiTuple<A, B> leftTuple, BiTuple<A, B> outTuple) {
        outTuple.setA(leftTuple.getA());
        outTuple.setB(leftTuple.getB());
    }

    @Override
    protected void updateOutTupleFromRight(BiTuple<A, B> rightTuple, BiTuple<A, B> outTuple) {
        outTuple.setA(rightTuple.getA());
        outTuple.setB(rightTuple.getB());
    }

}
