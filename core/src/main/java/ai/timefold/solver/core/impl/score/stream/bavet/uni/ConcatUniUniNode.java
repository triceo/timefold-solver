package ai.timefold.solver.core.impl.score.stream.bavet.uni;

import ai.timefold.solver.core.impl.score.stream.bavet.common.AbstractConcatNode;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.TupleLifecycle;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.UniTuple;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.UniversalTuple;

final class ConcatUniUniNode<A>
        extends AbstractConcatNode<UniTuple<A>, UniTuple<A>, UniTuple<A>> {

    ConcatUniUniNode(TupleLifecycle<UniTuple<A>> nextNodesTupleLifecycle,
            int inputStoreIndexLeftOutTupleList, int inputStoreIndexRightOutTupleList,
            int outputStoreSize) {
        super(nextNodesTupleLifecycle, inputStoreIndexLeftOutTupleList, inputStoreIndexRightOutTupleList,
                outputStoreSize);
    }

    @Override
    protected UniTuple<A> getOutTupleFromLeft(UniTuple<A> leftTuple) {
        return new UniversalTuple<>(leftTuple.getA(), outputStoreSize);
    }

    @Override
    protected UniTuple<A> getOutTupleFromRight(UniTuple<A> rightTuple) {
        return new UniversalTuple<>(rightTuple.getA(), outputStoreSize);
    }

    @Override
    protected void updateOutTupleFromLeft(UniTuple<A> leftTuple, UniTuple<A> outTuple) {
        outTuple.setA(leftTuple.getA());
    }

    @Override
    protected void updateOutTupleFromRight(UniTuple<A> rightTuple, UniTuple<A> outTuple) {
        outTuple.setA(rightTuple.getA());
    }

}
