package ai.timefold.solver.core.impl.bavet.quad;

import ai.timefold.solver.core.impl.bavet.common.AbstractIndexedJoinNode;
import ai.timefold.solver.core.impl.bavet.common.index.IndexerFactory;
import ai.timefold.solver.core.impl.bavet.common.tuple.InOutTupleStorePositionTracker;
import ai.timefold.solver.core.impl.bavet.common.tuple.QuadTuple;
import ai.timefold.solver.core.impl.bavet.common.tuple.TriTuple;
import ai.timefold.solver.core.impl.bavet.common.tuple.TupleLifecycle;
import ai.timefold.solver.core.impl.bavet.common.tuple.UniTuple;

public final class IndexedJoinQuadNode<A, B, C, D>
        extends AbstractIndexedJoinNode<TriTuple<A, B, C>, D, QuadTuple<A, B, C, D>> {

    public IndexedJoinQuadNode(IndexerFactory<D> indexerFactory, TupleLifecycle<QuadTuple<A, B, C, D>> nextNodesTupleLifecycle,
            InOutTupleStorePositionTracker positionTracker) {
        super(indexerFactory.buildTriLeftKeysExtractor(), indexerFactory, nextNodesTupleLifecycle,
                positionTracker);
    }

    @Override
    protected QuadTuple<A, B, C, D> createOutTuple(TriTuple<A, B, C> leftTuple, UniTuple<D> rightTuple) {
        return QuadTuple.of(leftTuple.getA(), leftTuple.getB(), leftTuple.getC(), rightTuple.getA(),
                outputStoreSizeTracker.computeStoreSize());
    }

    @Override
    protected void setOutTupleLeftFacts(QuadTuple<A, B, C, D> outTuple, TriTuple<A, B, C> leftTuple) {
        outTuple.setA(leftTuple.getA());
        outTuple.setB(leftTuple.getB());
        outTuple.setC(leftTuple.getC());
    }

    @Override
    protected void setOutTupleRightFact(QuadTuple<A, B, C, D> outTuple, UniTuple<D> rightTuple) {
        outTuple.setD(rightTuple.getA());
    }

}
