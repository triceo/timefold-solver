package ai.timefold.solver.core.impl.score.stream.bavet.uni;

import java.util.function.BiPredicate;
import java.util.function.Function;

import ai.timefold.solver.core.impl.score.stream.bavet.common.AbstractIndexedIfExistsNode;
import ai.timefold.solver.core.impl.score.stream.bavet.common.ExistsCounter;
import ai.timefold.solver.core.impl.score.stream.bavet.common.index.IndexProperties;
import ai.timefold.solver.core.impl.score.stream.bavet.common.index.Indexer;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.TupleLifecycle;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.UniTuple;

final class IndexedIfExistsUniNode<A, B> extends AbstractIndexedIfExistsNode<UniTuple<A>, B> {

    private final Function<A, IndexProperties> mappingA;
    private final BiPredicate<A, B> filtering;

    public IndexedIfExistsUniNode(boolean shouldExist,
            Function<A, IndexProperties> mappingA, Function<B, IndexProperties> mappingB,
            int inputStoreIndexLeftProperties, int inputStoreIndexRightProperties,
            TupleLifecycle<UniTuple<A>> nextNodesTupleLifecycle,
            Indexer<ExistsCounter<UniTuple<A>>> indexerA, Indexer<UniTuple<B>> indexerB) {
        this(shouldExist, mappingA, mappingB,
                inputStoreIndexLeftProperties, inputStoreIndexRightProperties,
                nextNodesTupleLifecycle, indexerA, indexerB, null);
    }

    public IndexedIfExistsUniNode(boolean shouldExist,
            Function<A, IndexProperties> mappingA, Function<B, IndexProperties> mappingB,
            int inputStoreIndexLeft, int inputStoreIndexRight,
            TupleLifecycle<UniTuple<A>> nextNodesTupleLifecycle,
            Indexer<ExistsCounter<UniTuple<A>>> indexerA, Indexer<UniTuple<B>> indexerB,
            BiPredicate<A, B> filtering) {
        super(shouldExist, mappingB,
                inputStoreIndexLeft, inputStoreIndexRight,
                nextNodesTupleLifecycle, indexerA, indexerB, filtering != null);
        this.mappingA = mappingA;
        this.filtering = filtering;
    }

    @Override
    protected IndexProperties createIndexProperties(UniTuple<A> leftTuple) {
        return mappingA.apply(leftTuple.factA);
    }

    @Override
    protected boolean testFiltering(UniTuple<A> leftTuple, UniTuple<B> rightTuple) {
        return filtering.test(leftTuple.factA, rightTuple.factA);
    }

}
