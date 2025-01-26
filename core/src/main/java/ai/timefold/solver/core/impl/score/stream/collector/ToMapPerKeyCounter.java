package ai.timefold.solver.core.impl.score.stream.collector;

import java.util.function.BinaryOperator;
import java.util.stream.Stream;

import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;

public final class ToMapPerKeyCounter<Value_> {

    private static final long DEFAULT_RETURN_VALUE = 0L;

    private final Object2LongLinkedOpenHashMap<Value_> counts = new Object2LongLinkedOpenHashMap<>();

    public ToMapPerKeyCounter() {
        counts.defaultReturnValue(DEFAULT_RETURN_VALUE);
    }

    public long add(Value_ value) {
        return counts.addTo(value, 1) + 1;
    }

    public long remove(Value_ value) {
        var count = counts.addTo(value, -1) - 1;
        if (count == 0) {
            counts.removeLong(value);
        }
        return count;
    }

    public Value_ merge(BinaryOperator<Value_> mergeFunction) {
        // Rebuilding the value from the collection is not incremental.
        // The impact is negligible, assuming there are not too many values for the same key.
        return counts.object2LongEntrySet()
                .stream()
                .map(e -> Stream.generate(e::getKey)
                        .limit(e.getLongValue())
                        .reduce(mergeFunction)
                        .orElseThrow(() -> new IllegalStateException("Impossible state: Should have had at least one value.")))
                .reduce(mergeFunction)
                .orElseThrow(() -> new IllegalStateException("Impossible state: Should have had at least one value."));
    }

    public boolean isEmpty() {
        return counts.isEmpty();
    }

}
