package ai.timefold.solver.core.impl.score.stream.bavet.common.index;

import java.util.function.Function;

import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.AbstractTuple;

/**
 * Represents a function which extracts index keys from a tuple.
 *
 * @param <Tuple_>
 */
@FunctionalInterface
public interface KeysExtractor<Tuple_ extends AbstractTuple> extends Function<Tuple_, Object> {
}
