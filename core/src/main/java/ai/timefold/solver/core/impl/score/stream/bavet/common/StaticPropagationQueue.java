package ai.timefold.solver.core.impl.score.stream.bavet.common;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.Tuple;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.TupleLifecycle;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.TupleState;

/**
 * The implementation moves tuples directly into an either retract, update or insert queue,
 * without any option of moving between the queues.
 * This is the most efficient implementation.
 * It will throw exceptions if a tuple is in the wrong queue, based on its state.
 *
 * @param <Tuple_>
 */
public final class StaticPropagationQueue<Tuple_ extends Tuple>
        implements PropagationQueue<Tuple_> {

    private final Deque<Tuple_> retractQueue;
    private final Deque<Tuple_> updateQueue;
    private final Deque<Tuple_> insertQueue;
    private final Consumer<Tuple_> retractPropagator;
    private final Consumer<Tuple_> updatePropagator;
    private final Consumer<Tuple_> insertPropagator;

    public StaticPropagationQueue(TupleLifecycle<Tuple_> nextNodesTupleLifecycle, int size) {
        // Guesstimate that updates are dominant.
        this.retractQueue = new ArrayDeque<>(size / 20);
        this.updateQueue = new ArrayDeque<>((size / 20) * 18);
        this.insertQueue = new ArrayDeque<>(size / 20);
        // Don't create these lambdas over and over again.
        this.retractPropagator = nextNodesTupleLifecycle::retract;
        this.updatePropagator = nextNodesTupleLifecycle::update;
        this.insertPropagator = nextNodesTupleLifecycle::insert;
    }

    public StaticPropagationQueue(TupleLifecycle<Tuple_> nextNodesTupleLifecycle) {
        this(nextNodesTupleLifecycle, 1000);
    }

    @Override
    public void insert(Tuple_ carrier) {
        if (carrier.getState() == TupleState.CREATING) {
            throw new IllegalStateException("Impossible state: The tuple (" + carrier + ") is already in the insert queue.");
        }
        carrier.setState(TupleState.CREATING);
        insertQueue.add(carrier);
    }

    @Override
    public void update(Tuple_ carrier) {
        if (carrier.getState() == TupleState.UPDATING) { // Skip double updates.
            return;
        }
        carrier.setState(TupleState.UPDATING);
        updateQueue.add(carrier);
    }

    @Override
    public void retract(Tuple_ carrier, TupleState newState) {
        var currentState = carrier.getState();
        if (currentState == newState) { // Skip double retracts.
            return;
        }
        if (newState.isActive() || newState == TupleState.DEAD) {
            throw new IllegalArgumentException("Impossible state: The state (" + newState + ") is not a valid retract state.");
        } else if (currentState == TupleState.ABORTING || currentState == TupleState.DYING) {
            throw new IllegalStateException("Impossible state: The tuple (" + carrier + ") is already in the retract queue.");
        }
        carrier.setState(newState);
        retractQueue.add(carrier);
    }

    @Override
    public void propagateRetracts() {
        if (retractQueue.isEmpty()) {
            return;
        }
        for (Tuple_ tuple : retractQueue) {
            switch (tuple.getState()) {
                case DYING -> propagate(tuple, retractPropagator, TupleState.DEAD);
                case ABORTING -> tuple.setState(TupleState.DEAD);
            }
        }
        retractQueue.clear();
    }

    private void propagate(Tuple_ tuple, Consumer<Tuple_> propagator, TupleState tupleState) {
        // Change state before propagation, so that the next node can't make decisions on the original state.
        tuple.setState(tupleState);
        propagator.accept(tuple);
    }

    @Override
    public void propagateUpdates() {
        processAndClear(updateQueue, updatePropagator);
    }

    private void processAndClear(Deque<Tuple_> dirtyQueue, Consumer<Tuple_> propagator) {
        if (dirtyQueue.isEmpty()) {
            return;
        }
        for (Tuple_ tuple : dirtyQueue) {
            if (tuple.getState() == TupleState.DEAD) {
                /*
                 * DEAD signifies the tuple was both in insert/update and retract queues.
                 * This happens when a tuple was inserted/updated and subsequently retracted, all before propagation.
                 * We can safely ignore the later insert/update,
                 * as by this point the more recent retract has already been processed,
                 * setting the state to DEAD.
                 */
                continue;
            }
            propagate(tuple, propagator, TupleState.OK);
        }
        dirtyQueue.clear();
    }

    @Override
    public void propagateInserts() {
        processAndClear(insertQueue, insertPropagator);
        if (!retractQueue.isEmpty()) {
            throw new IllegalStateException("Impossible state: The retract queue (" + retractQueue + ") is not empty.");
        } else if (!updateQueue.isEmpty()) {
            throw new IllegalStateException("Impossible state: The update queue (" + updateQueue + ") is not empty.");
        }
    }

}
