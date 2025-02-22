package ai.timefold.solver.core.impl.score.stream.collector.uni;

import java.util.function.Supplier;
import java.util.function.ToLongFunction;

import ai.timefold.solver.core.impl.score.stream.collector.LongSumCalculator;

import org.jspecify.annotations.NonNull;

final class SumLongUniCollector<A> extends LongCalculatorUniCollector<A, Long, LongSumCalculator> {
    SumLongUniCollector(ToLongFunction<? super A> mapper) {
        super(mapper);
    }

    @Override
    public @NonNull Supplier<LongSumCalculator> supplier() {
        return LongSumCalculator::new;
    }
}
