package ai.timefold.solver.core.impl.move;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A supply whose internal state changes are recorded in an {@link UndoJournal}.
 * The journal calls {@link #undo} in reverse order of recording.
 */
@NullMarked
public interface UndoableSupply {

    /**
     * Reverts one recorded change.
     * The arguments are those given to {@link UndoJournal#recordSupplyChange}.
     */
    void undo(Object a, @Nullable Object b, int i);

}
