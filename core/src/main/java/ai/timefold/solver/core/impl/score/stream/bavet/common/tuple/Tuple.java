package ai.timefold.solver.core.impl.score.stream.bavet.common.tuple;

import java.util.function.Function;

import ai.timefold.solver.core.api.score.stream.uni.UniConstraintStream;

/**
 * A tuple is an <i>out tuple</i> in exactly one node and an <i>in tuple</i> in one or more nodes.
 *
 * <p>
 * A tuple must not implement equals()/hashCode() to fact equality,
 * because some stream operations ({@link UniConstraintStream#map(Function)}, ...)
 * might create 2 different tuple instances to contain the same facts
 * and because a tuple's origin may replace a tuple's fact.
 *
 * <p>
 * A tuple is modifiable.
 * However, only the origin node of a tuple (the node where the tuple is the out tuple) may modify it.
 */
public sealed interface Tuple permits UniTuple {

    TupleState getState();

    void setState(TupleState state);

    <Value_> Value_ getStore(int index);

    void setStore(int index, Object value);

    <Value_> Value_ removeStore(int index);

}
