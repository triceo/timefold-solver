package ai.timefold.solver.core.impl.bavet.uni;

import java.util.Objects;
import java.util.function.Predicate;

import ai.timefold.solver.core.impl.bavet.common.tuple.TupleLifecycle;
import ai.timefold.solver.core.impl.bavet.common.tuple.UniTuple;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class ForEachFilteredUniNode<A>
        extends AbstractForEachUniNode<A> {

    private final TupleLifecycle<UniTuple<A>> nextNodesTupleLifecycle;
    private final Predicate<A> filter;
    /**
     * Counts every fact currently inserted into this node, including those which do not pass the filter.
     * Only {@link #insertFact(Object)} and {@link #retractFact(Object, UniTuple)} may mutate it;
     * the filter transitions inside {@link #updateFact(Object, UniTuple)} concern facts already counted.
     */
    private int tupleCountWithoutFiltering = 0;

    public ForEachFilteredUniNode(Class<A> forEachClass, Predicate<A> filter,
            TupleLifecycle<UniTuple<A>> nextNodesTupleLifecycle, boolean indictmentsEnabled, int outputStoreSize) {
        super(forEachClass, nextNodesTupleLifecycle, indictmentsEnabled, outputStoreSize);
        this.nextNodesTupleLifecycle = Objects.requireNonNull(nextNodesTupleLifecycle);
        this.filter = Objects.requireNonNull(filter);
    }

    @Override
    public void afterAllFactsInserted(boolean unused) {
        nextNodesTupleLifecycle.afterAllFactsInserted(tupleCountWithoutFiltering > 0);
    }

    @Override
    public boolean isActive() {
        // The input may change during update,
        // and therefore the filter may let things propagate which it previously did not.
        // For this reason, this node must be considered active if it saw at least one input;
        // only with zero tuples can it be considered inactive, as the filter has nothing to propagate.
        return tupleCountWithoutFiltering > 0 && nextNodesTupleLifecycle.isActive();
    }

    @Override
    public @Nullable UniTuple<A> insertFact(@Nullable A a) {
        tupleCountWithoutFiltering++;
        return filter.test(a) ? insertTuple(a) : null;
    }

    @Override
    public @Nullable UniTuple<A> updateFact(@Nullable A a, @Nullable UniTuple<A> tuple) {
        if (tuple == null) { // The fact did not pass the filter before.
            return filter.test(a) ? insertTuple(a) : null;
        } else if (filter.test(a)) {
            updateExisting(a, tuple);
            return tuple;
        } else { // Tuple no longer passes the filter; the fact itself remains inserted.
            retractExisting(a, tuple);
            return null;
        }
    }

    @Override
    public void retractFact(@Nullable A a, @Nullable UniTuple<A> tuple) {
        tupleCountWithoutFiltering--;
        if (tuple != null) {
            retractExisting(a, tuple);
        }
    }

}
