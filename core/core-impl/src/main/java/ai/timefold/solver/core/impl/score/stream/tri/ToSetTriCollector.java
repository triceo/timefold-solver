package ai.timefold.solver.core.impl.score.stream.tri;

import java.util.Set;
import java.util.function.Supplier;

import ai.timefold.solver.core.api.function.TriFunction;
import ai.timefold.solver.core.impl.score.stream.SetUndoableActionable;

final class ToSetTriCollector<A, B, C, Mapped_>
        extends UndoableActionableTriCollector<A, B, C, Mapped_, Set<Mapped_>, SetUndoableActionable<Mapped_>> {
    ToSetTriCollector(TriFunction<? super A, ? super B, ? super C, ? extends Mapped_> mapper) {
        super(mapper);
    }

    @Override
    public Supplier<SetUndoableActionable<Mapped_>> supplier() {
        return SetUndoableActionable::new;
    }
}
