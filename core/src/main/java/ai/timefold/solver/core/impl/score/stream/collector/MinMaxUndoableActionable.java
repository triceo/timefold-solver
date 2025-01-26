package ai.timefold.solver.core.impl.score.stream.collector;

import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Function;

import ai.timefold.solver.core.impl.util.ConstantLambdaUtils;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;

public final class MinMaxUndoableActionable<Result_, Property_> implements UndoableActionable<Result_, Result_> {
    private final boolean isMin;
    private final NavigableMap<Property_, Object2IntLinkedOpenHashMap<Result_>> propertyToItemCountMap;
    private final Function<? super Result_, ? extends Property_> propertyFunction;

    private MinMaxUndoableActionable(boolean isMin,
            NavigableMap<Property_, Object2IntLinkedOpenHashMap<Result_>> propertyToItemCountMap,
            Function<? super Result_, ? extends Property_> propertyFunction) {
        this.isMin = isMin;
        this.propertyToItemCountMap = propertyToItemCountMap;
        this.propertyFunction = propertyFunction;
    }

    public static <Result extends Comparable<? super Result>> MinMaxUndoableActionable<Result, Result> minCalculator() {
        return new MinMaxUndoableActionable<>(true, new TreeMap<>(), ConstantLambdaUtils.identity());
    }

    public static <Result extends Comparable<? super Result>> MinMaxUndoableActionable<Result, Result> maxCalculator() {
        return new MinMaxUndoableActionable<>(false, new TreeMap<>(), ConstantLambdaUtils.identity());
    }

    public static <Result> MinMaxUndoableActionable<Result, Result> minCalculator(Comparator<? super Result> comparator) {
        return new MinMaxUndoableActionable<>(true, new TreeMap<>(comparator), ConstantLambdaUtils.identity());
    }

    public static <Result> MinMaxUndoableActionable<Result, Result> maxCalculator(Comparator<? super Result> comparator) {
        return new MinMaxUndoableActionable<>(false, new TreeMap<>(comparator), ConstantLambdaUtils.identity());
    }

    public static <Result, Property extends Comparable<? super Property>> MinMaxUndoableActionable<Result, Property>
            minCalculator(
                    Function<? super Result, ? extends Property> propertyMapper) {
        return new MinMaxUndoableActionable<>(true, new TreeMap<>(), propertyMapper);
    }

    public static <Result, Property extends Comparable<? super Property>> MinMaxUndoableActionable<Result, Property>
            maxCalculator(
                    Function<? super Result, ? extends Property> propertyMapper) {
        return new MinMaxUndoableActionable<>(false, new TreeMap<>(), propertyMapper);
    }

    @Override
    public Runnable insert(Result_ item) {
        var key = propertyFunction.apply(item);
        var itemCountMap = propertyToItemCountMap.computeIfAbsent(key, ignored -> new Object2IntLinkedOpenHashMap<>());
        itemCountMap.addTo(item, 1);
        return () -> {
            if (itemCountMap.addTo(item, -1) == 1) {
                itemCountMap.removeInt(item);
                if (itemCountMap.isEmpty()) {
                    propertyToItemCountMap.remove(key);
                }
            }
        };
    }

    @Override
    public Result_ result() {
        if (propertyToItemCountMap.isEmpty()) {
            return null;
        }
        var propertyToItemCountEntry = isMin ? propertyToItemCountMap.firstEntry() : propertyToItemCountMap.lastEntry();
        return getFirstKey(propertyToItemCountEntry.getValue());
    }

    private static <Key_> Key_ getFirstKey(Map<Key_, ?> map) {
        return map.keySet().iterator().next();
    }
}
