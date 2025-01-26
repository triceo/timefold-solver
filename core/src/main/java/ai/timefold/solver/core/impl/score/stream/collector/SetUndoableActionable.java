package ai.timefold.solver.core.impl.score.stream.collector;

import java.util.Set;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;

public final class SetUndoableActionable<Mapped_> implements UndoableActionable<Mapped_, Set<Mapped_>> {

    private final Object2IntLinkedOpenHashMap<Mapped_> itemToCount = new Object2IntLinkedOpenHashMap<>();

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
    public Set<Mapped_> result() {
        return itemToCount.keySet();
    }
}
