package ai.timefold.solver.core.impl.score.stream.bavet.bi;

import java.util.function.BiPredicate;

import ai.timefold.solver.core.impl.score.stream.bavet.common.AbstractUnindexedJoinNode;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.BiTuple;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.TupleLifecycle;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.UniTuple;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.UniversalTuple;

final class UnindexedJoinBiNode<A, B>
        extends AbstractUnindexedJoinNode<UniTuple<A>, B, BiTuple<A, B>> {

    private final BiPredicate<A, B> filtering;
    private final int outputStoreSize;

    public UnindexedJoinBiNode(
            int inputStoreIndexLeftEntry, int inputStoreIndexLeftOutTupleList,
            int inputStoreIndexRightEntry, int inputStoreIndexRightOutTupleList,
            TupleLifecycle<BiTuple<A, B>> nextNodesTupleLifecycle, BiPredicate<A, B> filtering,
            int outputStoreSize,
            int outputStoreIndexLeftOutEntry, int outputStoreIndexRightOutEntry) {
        super(inputStoreIndexLeftEntry, inputStoreIndexLeftOutTupleList,
                inputStoreIndexRightEntry, inputStoreIndexRightOutTupleList,
                nextNodesTupleLifecycle, filtering != null,
                outputStoreIndexLeftOutEntry, outputStoreIndexRightOutEntry);
        this.filtering = filtering;
        this.outputStoreSize = outputStoreSize;
    }

    @Override
    protected BiTuple<A, B> createOutTuple(UniTuple<A> leftTuple, UniTuple<B> rightTuple) {
        return new UniversalTuple<>(leftTuple.getA(), rightTuple.getA(), outputStoreSize);
    }

    @Override
    protected void setOutTupleLeftFacts(BiTuple<A, B> outTuple, UniTuple<A> leftTuple) {
        outTuple.setA(leftTuple.getA());
    }

    @Override
    protected void setOutTupleRightFact(BiTuple<A, B> outTuple, UniTuple<B> rightTuple) {
        outTuple.setB(rightTuple.getA());
    }

    @Override
    protected boolean testFiltering(UniTuple<A> leftTuple, UniTuple<B> rightTuple) {
        return filtering.test(leftTuple.getA(), rightTuple.getA());
    }

}
