package ai.timefold.solver.core.impl.score.stream.collector;

import java.util.Map;

import ai.timefold.solver.core.impl.util.ListBasedScalingMap;
import ai.timefold.solver.core.impl.util.MutableInt;

public final class IntDistinctCountCalculator<Input_> implements ObjectCalculator<Input_, Integer, Input_> {

    private final Map<Input_, MutableInt> countMap = ListBasedScalingMap.create();

    @Override
    public Input_ insert(Input_ input) {
        countMap.computeIfAbsent(input, ignored -> new MutableInt()).increment();
        return input;
    }

    @Override
    public void retract(Input_ mapped) {
        if (countMap.get(mapped).decrement() == 0) {
            countMap.remove(mapped);
        }
    }

    @Override
    public Integer result() {
        return countMap.size();
    }
}
