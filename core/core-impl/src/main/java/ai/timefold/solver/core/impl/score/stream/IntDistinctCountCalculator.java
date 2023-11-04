package ai.timefold.solver.core.impl.score.stream;

import java.util.HashMap;
import java.util.Map;

import ai.timefold.solver.core.impl.util.MutableInt;

public final class IntDistinctCountCalculator<Input_> implements ObjectCalculator<Input_, Integer> {
    private final Map<Input_, MutableInt> countMap = new HashMap<>();

    @Override
    public void insert(Input_ input) {
        countMap.computeIfAbsent(input, ignored -> new MutableInt()).increment();
    }

    @Override
    public void retract(Input_ input) {
        if (countMap.get(input).decrement() == 0) {
            countMap.remove(input);
        }
    }

    @Override
    public Integer result() {
        return countMap.size();
    }
}
