package ai.timefold.solver.core.impl.score.stream.collector;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

public final class LongDistinctCountCalculator<Input_> implements ObjectCalculator<Input_, Long, Input_> {

    private final Object2LongOpenHashMap<Input_> countMap = new Object2LongOpenHashMap<>();

    @Override
    public Input_ insert(Input_ input) {
        countMap.addTo(input, 1);
        return input;
    }

    @Override
    public void retract(Input_ mapped) {
        var oldValue = countMap.addTo(mapped, -1);
        if (oldValue == 1) {
            countMap.removeLong(mapped);
        }
    }

    @Override
    public Long result() {
        return (long) countMap.size();
    }
}
