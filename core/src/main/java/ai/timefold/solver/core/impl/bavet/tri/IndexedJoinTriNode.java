package ai.timefold.solver.core.impl.bavet.tri;

import ai.timefold.solver.core.impl.bavet.common.AbstractIndexedJoinNode;
import ai.timefold.solver.core.impl.bavet.common.index.IndexerFactory;
import ai.timefold.solver.core.impl.bavet.common.tuple.BiTuple;
import ai.timefold.solver.core.impl.bavet.common.tuple.InOutTupleStorePositionTracker;
import ai.timefold.solver.core.impl.bavet.common.tuple.TriTuple;
import ai.timefold.solver.core.impl.bavet.common.tuple.TupleLifecycle;
import ai.timefold.solver.core.impl.bavet.common.tuple.UniTuple;

public final class IndexedJoinTriNode<A, B, C>
        extends AbstractIndexedJoinNode<BiTuple<A, B>, C, TriTuple<A, B, C>> {

    public IndexedJoinTriNode(IndexerFactory<C> indexerFactory, TupleLifecycle<TriTuple<A, B, C>> nextNodesTupleLifecycle,
            InOutTupleStorePositionTracker tupleStorePositionTracker) {
        super(indexerFactory.buildBiLeftKeysExtractor(), indexerFactory, nextNodesTupleLifecycle,
                tupleStorePositionTracker);
    }

    @Override
    protected TriTuple<A, B, C> createOutTuple(BiTuple<A, B> leftTuple, UniTuple<C> rightTuple) {
        return TriTuple.of(leftTuple.getA(), leftTuple.getB(), rightTuple.getA(), outputStoreSizeTracker.computeStoreSize());
    }

    @Override
    protected void setOutTupleLeftFacts(TriTuple<A, B, C> outTuple, BiTuple<A, B> leftTuple) {
        outTuple.setA(leftTuple.getA());
        outTuple.setB(leftTuple.getB());
    }

    @Override
    protected void setOutTupleRightFact(TriTuple<A, B, C> outTuple, UniTuple<C> rightTuple) {
        outTuple.setC(rightTuple.getA());
    }

}
