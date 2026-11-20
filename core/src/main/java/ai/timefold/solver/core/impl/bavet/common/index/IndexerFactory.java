package ai.timefold.solver.core.impl.bavet.common.index;

import ai.timefold.solver.core.impl.bavet.common.joiner.AbstractJoiner;
import ai.timefold.solver.core.impl.bavet.common.tuple.InTupleStorePositionTracker;

/**
 * The default {@link AbstractIndexerFactory}:
 * every node gets a {@link TwoIndexerJoinIndex} over chains of the indexers of this package.
 *
 * @param <Right_>
 */
public final class IndexerFactory<Right_> extends AbstractIndexerFactory<Right_> {

    public IndexerFactory(AbstractJoiner<Right_> joiner) {
        super(joiner);
    }

    @Override
    public <L, R> JoinIndex<L, R> buildJoinIndex(InTupleStorePositionTracker tupleStorePositionTracker) {
        return new TwoIndexerJoinIndex<>(buildIndexer(true), buildIndexer(false));
    }

}
