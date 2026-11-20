package ai.timefold.solver.core.impl.bavet.uni;

import ai.timefold.solver.core.impl.bavet.common.AbstractRootNode;
import ai.timefold.solver.core.impl.bavet.common.Propagator;
import ai.timefold.solver.core.impl.bavet.common.StaticPropagationQueue;
import ai.timefold.solver.core.impl.bavet.common.StreamKind;
import ai.timefold.solver.core.impl.bavet.common.tuple.TupleLifecycle;
import ai.timefold.solver.core.impl.bavet.common.tuple.TupleState;
import ai.timefold.solver.core.impl.bavet.common.tuple.UniTuple;
import ai.timefold.solver.core.impl.bavet.common.tuple.indictment.IndictmentSource;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Filtering nodes are expensive.
 * Considering that most streams start with a nullity check on genuine planning variables,
 * it makes sense to create a specialized version of the node for this case ({@link ForEachFilteredUniNode}),
 * as opposed to forcing an extra filter node on the generic case ({@link ForEachUnfilteredUniNode}).
 *
 * @param <A>
 */
@NullMarked
public abstract sealed class AbstractForEachUniNode<A>
        extends AbstractRootNode<A>
        permits ForEachFilteredUniNode, ForEachUnfilteredUniNode {

    private final Class<A> forEachClass;
    private final int outputStoreSize;
    private final StaticPropagationQueue<UniTuple<A>> propagationQueue;
    private final boolean indictmentsEnabled;

    protected AbstractForEachUniNode(Class<A> forEachClass, TupleLifecycle<UniTuple<A>> nextNodesTupleLifecycle,
            boolean indictmentsEnabled, int outputStoreSize) {
        this.forEachClass = forEachClass;
        this.outputStoreSize = outputStoreSize;
        this.indictmentsEnabled = indictmentsEnabled;
        this.propagationQueue = new StaticPropagationQueue<>(nextNodesTupleLifecycle);
    }

    @Override
    public StreamKind getStreamKind() {
        return StreamKind.FOR_EACH;
    }

    @Override
    public boolean allowsInstancesOf(Class<?> clazz) {
        return forEachClass.isAssignableFrom(clazz);
    }

    @Override
    public Class<?>[] getSourceClasses() {
        return new Class[] { forEachClass };
    }

    /**
     * @return the tuple of the fact, or null if this node does not hold one for it
     */
    public abstract @Nullable UniTuple<A> insertFact(@Nullable A a);

    /**
     * @param tuple the tuple which {@link #insertFact(Object)} or a previous call of this method returned
     * @return the tuple to keep for the fact from now on
     */
    public abstract @Nullable UniTuple<A> updateFact(@Nullable A a, @Nullable UniTuple<A> tuple);

    /**
     * @param tuple the tuple which {@link #insertFact(Object)} or {@link #updateFact(Object, UniTuple)} last returned
     */
    public abstract void retractFact(@Nullable A a, @Nullable UniTuple<A> tuple);

    protected final UniTuple<A> insertTuple(@Nullable A a) {
        var tuple = UniTuple.of(a, outputStoreSize);
        if (indictmentsEnabled) {
            tuple.setIndictmentSource(IndictmentSource.of(a));
        }
        propagationQueue.insert(tuple);
        return tuple;
    }

    protected final void updateExisting(@Nullable A a, UniTuple<A> tuple) {
        var state = tuple.getState();
        if (state.isDirty()) {
            if (state == TupleState.DYING || state == TupleState.ABORTING) {
                throw new IllegalStateException("The fact (%s) was already retracted."
                        .formatted(a));
            }
            // CREATING or UPDATING is ignored; it's already in the queue.
        } else {
            propagationQueue.update(tuple);
        }
    }

    protected void retractExisting(@Nullable A a, UniTuple<A> tuple) {
        var state = tuple.getState();
        if (state.isDirty()) {
            if (state == TupleState.DYING || state == TupleState.ABORTING) {
                throw new IllegalStateException("The fact (%s) was already retracted."
                        .formatted(a));
            }
            propagationQueue.retract(tuple, state == TupleState.CREATING ? TupleState.ABORTING : TupleState.DYING);
        } else {
            propagationQueue.retract(tuple, TupleState.DYING);
        }
    }

    @Override
    public Propagator getPropagator() {
        return propagationQueue;
    }

    public final Class<A> getForEachClass() {
        return forEachClass;
    }

    @Override
    public final String toString() {
        return "%s(%s)"
                .formatted(getClass().getSimpleName(), forEachClass.getSimpleName());
    }

}
