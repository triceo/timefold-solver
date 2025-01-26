package ai.timefold.solver.core.impl.score.stream.collector;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public final class IntDistinctCountCalculator<Input_> implements ObjectCalculator<Input_, Integer, Input_> {

    private final Object2IntOpenHashMap<Input_> countMap = new Object2IntOpenHashMap<>();

    @Override
    public Input_ insert(Input_ input) {
        countMap.addTo(input, 1);
        return input;
    }

    @Override
    public void retract(Input_ mapped) {
        var oldValue = countMap.addTo(mapped, -1);
        if (oldValue == 1) {
            countMap.removeInt(mapped);
        }
    }

    @Override
    public Integer result() {
        return countMap.size();
    }
}
