package ai.timefold.solver.core.impl.score.stream.bavet.common.tuple;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

import ai.timefold.solver.core.api.function.QuadPredicate;
import ai.timefold.solver.core.api.function.TriPredicate;

public interface TupleLifecycle<Tuple_ extends AbstractTuple> {

    static <Tuple_ extends AbstractTuple> TupleLifecycle<Tuple_> ofLeft(LeftTupleLifecycle<Tuple_> leftTupleLifecycle) {
        return new LeftTupleLifecycleImpl<>(leftTupleLifecycle);
    }

    static <Tuple_ extends AbstractTuple> TupleLifecycle<Tuple_> ofRight(RightTupleLifecycle<Tuple_> rightTupleLifecycle) {
        return new RightTupleLifecycleImpl<>(rightTupleLifecycle);
    }

    static <Tuple_ extends AbstractTuple> TupleLifecycle<Tuple_> of(TupleLifecycle<Tuple_>... tupleLifecycles) {
        return new AggregatedTupleLifecycle<>(tupleLifecycles);
    }

    static <A> TupleLifecycle<UniTuple<A>> conditionally(TupleLifecycle<UniTuple<A>> tupleLifecycle, Predicate<A> predicate) {
        return new ConditionalTupleLifecycle<>(tupleLifecycle, tuple -> predicate.test(tuple.factA));
    }

    static <A, B> TupleLifecycle<BiTuple<A, B>> conditionally(TupleLifecycle<BiTuple<A, B>> tupleLifecycle,
            BiPredicate<A, B> predicate) {
        return new ConditionalTupleLifecycle<>(tupleLifecycle, tuple -> predicate.test(tuple.factA, tuple.factB));
    }

    static <A, B, C> TupleLifecycle<TriTuple<A, B, C>> conditionally(TupleLifecycle<TriTuple<A, B, C>> tupleLifecycle,
            TriPredicate<A, B, C> predicate) {
        return new ConditionalTupleLifecycle<>(tupleLifecycle, tuple -> predicate.test(tuple.factA, tuple.factB, tuple.factC));
    }

    static <A, B, C, D> TupleLifecycle<QuadTuple<A, B, C, D>>
            conditionally(TupleLifecycle<QuadTuple<A, B, C, D>> tupleLifecycle, QuadPredicate<A, B, C, D> predicate) {
        return new ConditionalTupleLifecycle<>(tupleLifecycle,
                tuple -> predicate.test(tuple.factA, tuple.factB, tuple.factC, tuple.factD));
    }

    void insert(Tuple_ tuple);

    void update(Tuple_ tuple);

    void retract(Tuple_ tuple);

}
