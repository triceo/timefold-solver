package ai.timefold.solver.core.impl.score.stream.collector;

import java.util.Comparator;
import java.util.SortedSet;

import it.unimi.dsi.fastutil.objects.Object2IntRBTreeMap;

public final class SortedSetUndoableActionable<Mapped_> implements UndoableActionable<Mapped_, SortedSet<Mapped_>> {

    private final Object2IntRBTreeMap<Mapped_> itemToCount;

    private SortedSetUndoableActionable(Object2IntRBTreeMap<Mapped_> itemToCount) {
        this.itemToCount = itemToCount;
    }

    public static <Result> SortedSetUndoableActionable<Result> orderBy(Comparator<? super Result> comparator) {
        return new SortedSetUndoableActionable<>(new Object2IntRBTreeMap<>(comparator));
    }

    @Override
    public Runnable insert(Mapped_ result) {
        itemToCount.addTo(result, 1);
        return () -> {
            if (itemToCount.addTo(result, -1) == 1) {
                itemToCount.removeInt(result);
            }
        };
    }

    @Override
    public SortedSet<Mapped_> result() {
        return itemToCount.keySet();
    }
}
