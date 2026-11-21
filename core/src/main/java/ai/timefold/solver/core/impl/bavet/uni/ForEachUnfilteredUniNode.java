package ai.timefold.solver.core.impl.bavet.uni;

import ai.timefold.solver.core.impl.bavet.common.tuple.TupleLifecycle;
import ai.timefold.solver.core.impl.bavet.common.tuple.UniTuple;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class ForEachUnfilteredUniNode<A>
        extends AbstractForEachUniNode<A> {

    private final TupleLifecycle<UniTuple<A>> nextNodesTupleLifecycle;
    private boolean isActive;
    private int factCount = 0;

    public ForEachUnfilteredUniNode(Class<A> forEachClass, TupleLifecycle<UniTuple<A>> nextNodesTupleLifecycle,
            boolean indictmentsEnabled, int outputStoreSize) {
        super(forEachClass, nextNodesTupleLifecycle, indictmentsEnabled, outputStoreSize);
        this.nextNodesTupleLifecycle = nextNodesTupleLifecycle;
    }

    @Override
    public void afterAllFactsInserted(boolean unused) {
        isActive = factCount > 0;
        nextNodesTupleLifecycle.afterAllFactsInserted(isActive);
    }

    @Override
    public boolean isActive() {
        return isActive && nextNodesTupleLifecycle.isActive();
    }

    @Override
    public UniTuple<A> insertFact(@Nullable A a) {
        factCount++;
        return insertTuple(a);
    }

    @Override
    public UniTuple<A> updateFact(@Nullable A a, @Nullable UniTuple<A> tuple) {
        updateExisting(a, tuple);
        return tuple;
    }

    @Override
    public void retractFact(@Nullable A a, @Nullable UniTuple<A> tuple) {
        factCount--;
        retractExisting(a, tuple);
    }

}
