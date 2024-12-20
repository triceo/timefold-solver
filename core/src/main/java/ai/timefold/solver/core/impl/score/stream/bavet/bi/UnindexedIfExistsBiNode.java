package ai.timefold.solver.core.impl.score.stream.bavet.bi;

import ai.timefold.solver.core.api.function.TriPredicate;
import ai.timefold.solver.core.impl.score.stream.bavet.common.AbstractUnindexedIfExistsNode;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.BiTuple;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.TupleLifecycle;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.UniTuple;

final class UnindexedIfExistsBiNode<A, B, C> extends AbstractUnindexedIfExistsNode<BiTuple<A, B>, C> {

    private final TriPredicate<A, B, C> filtering;

    public UnindexedIfExistsBiNode(boolean shouldExist,
            int inputStoreIndexLeft, int inputStoreIndexRight,
            TupleLifecycle<BiTuple<A, B>> nextNodesTupleLifecycle) {
        this(shouldExist,
                inputStoreIndexLeft, inputStoreIndexRight,
                nextNodesTupleLifecycle, null);
    }

    public UnindexedIfExistsBiNode(boolean shouldExist,
            int inputStoreIndexLeft, int inputStoreIndexRight,
            TupleLifecycle<BiTuple<A, B>> nextNodesTupleLifecycle,
            TriPredicate<A, B, C> filtering) {
        super(shouldExist,
                inputStoreIndexLeft, inputStoreIndexRight,
                nextNodesTupleLifecycle, filtering != null);
        this.filtering = filtering;
    }

    @Override
    protected boolean testFiltering(BiTuple<A, B> leftTuple, UniTuple<C> rightTuple) {
        return filtering.test(leftTuple.factA, leftTuple.factB, rightTuple.factA);
    }

}
