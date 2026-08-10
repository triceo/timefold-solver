package ai.timefold.solver.core.impl.bavet.bi;

import ai.timefold.solver.core.impl.bavet.common.AbstractIndexedJoinNode;
import ai.timefold.solver.core.impl.bavet.common.index.IndexerFactory;
import ai.timefold.solver.core.impl.bavet.common.tuple.BiTuple;
import ai.timefold.solver.core.impl.bavet.common.tuple.InOutTupleStorePositionTracker;
import ai.timefold.solver.core.impl.bavet.common.tuple.TupleLifecycle;
import ai.timefold.solver.core.impl.bavet.common.tuple.UniTuple;

public final class IndexedJoinBiNode<A, B> extends AbstractIndexedJoinNode<UniTuple<A>, B, BiTuple<A, B>> {

    public IndexedJoinBiNode(IndexerFactory<B> indexerFactory, TupleLifecycle<BiTuple<A, B>> nextNodesTupleLifecycle,
            InOutTupleStorePositionTracker tupleStorePositionTracker) {
        super(indexerFactory.buildUniLeftKeysExtractor(), indexerFactory, nextNodesTupleLifecycle,
                tupleStorePositionTracker);
    }

    @Override
    protected BiTuple<A, B> createOutTuple(UniTuple<A> leftTuple, UniTuple<B> rightTuple) {
        return BiTuple.of(leftTuple.getA(), rightTuple.getA(), outputStoreSizeTracker.computeStoreSize());
    }

    @Override
    protected void setOutTupleLeftFacts(BiTuple<A, B> outTuple, UniTuple<A> leftTuple) {
        outTuple.setA(leftTuple.getA());
    }

    @Override
    protected void setOutTupleRightFact(BiTuple<A, B> outTuple, UniTuple<B> rightTuple) {
        outTuple.setB(rightTuple.getA());
    }

}
