package ai.timefold.solver.core.impl.move;

import java.util.Arrays;
import java.util.List;

import ai.timefold.solver.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import ai.timefold.solver.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import ai.timefold.solver.core.impl.domain.variable.descriptor.VariableDescriptor;
import ai.timefold.solver.core.impl.score.director.InnerScoreDirector;
import ai.timefold.solver.core.preview.api.move.Move;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Log of every change made while open; {@link #restore} reverts them in reverse order.
 * Records are forwarded to the parent journal; in genuine-only mode, restore replays genuine changes.
 *
 * @param <Solution_> the solution type
 */
@NullMarked
public final class UndoJournal<Solution_> {

    private static final byte VARIABLE = 0;
    private static final byte LIST = 1;
    private static final byte BEFORE_ASSIGNED = 2;
    private static final byte AFTER_ASSIGNED = 3;
    private static final byte BEFORE_UNASSIGNED = 4;
    private static final byte AFTER_UNASSIGNED = 5;
    private static final byte SUPPLY = 6;
    private static final int INITIAL_CAPACITY = 16;

    private final @Nullable UndoJournal<Solution_> parent;

    // Parallel arrays; one slot per entry.
    // first: descriptor or supply; second: entity, element or a; third: old value, old range or b.
    private byte[] kinds = new byte[INITIAL_CAPACITY];
    private @Nullable Object[] firsts = new Object[INITIAL_CAPACITY];
    private @Nullable Object[] seconds = new Object[INITIAL_CAPACITY];
    private @Nullable Object[] thirds = new Object[INITIAL_CAPACITY];
    private int[] fromIndexes = new int[INITIAL_CAPACITY];
    private int[] sizes = new int[INITIAL_CAPACITY];
    private int size = 0;

    // Open beforeListVariableChanged brackets: entity, fromIndex, toIndex and list size.
    private @Nullable Object[] bracketEntities = new Object[2];
    private int[] bracketValues = new int[6];
    private int bracketCount = 0;

    private int parentMark = 0;
    private boolean muted = false;
    private boolean genuineOnly = false;
    private boolean hasCapturedState = false;
    private @Nullable Object variableSupportUndoState;
    private boolean lastVariableUpdateSuccessful = true;

    public UndoJournal(@Nullable UndoJournal<Solution_> parent) {
        this.parent = parent;
    }

    public @Nullable UndoJournal<Solution_> getParent() {
        return parent;
    }

    /**
     * Clears the journal and sets its mode and the state to write back on {@link #restore}.
     *
     * @param genuineOnly true for models with declarative shadow variables
     * @param variableSupportUndoState ignored if genuineOnly; otherwise the snapshot to write back on restore
     * @param lastVariableUpdateSuccessful written back on restore, unless genuineOnly
     */
    public void begin(boolean genuineOnly, @Nullable Object variableSupportUndoState, boolean lastVariableUpdateSuccessful) {
        clear();
        this.parentMark = parent == null ? 0 : parent.size;
        this.genuineOnly = genuineOnly;
        this.hasCapturedState = !genuineOnly;
        this.variableSupportUndoState = variableSupportUndoState;
        this.lastVariableUpdateSuccessful = lastVariableUpdateSuccessful;
    }

    /**
     * Call before a basic or shadow variable changes; reads the old value from the entity.
     */
    public void recordVariable(VariableDescriptor<Solution_> variableDescriptor, Object entity) {
        if (muted) {
            return;
        }
        if (!genuineOnly || variableDescriptor instanceof GenuineVariableDescriptor<Solution_>) {
            var index = append(VARIABLE);
            firsts[index] = variableDescriptor;
            seconds[index] = entity;
            thirds[index] = variableDescriptor.getValue(entity);
        }
        if (parent != null) {
            parent.recordVariable(variableDescriptor, entity);
        }
    }

    /**
     * Call before the range [fromIndex, toIndex) of a list variable changes.
     *
     * @throws IllegalArgumentException if the entity already has an open bracket
     */
    public void recordList(ListVariableDescriptor<Solution_> variableDescriptor, Object entity, int fromIndex,
            int toIndex) {
        if (muted) {
            return;
        }
        var list = variableDescriptor.getValue(entity);
        openBracket(entity, fromIndex, toIndex, list.size());
        appendList(variableDescriptor, entity, fromIndex, toIndex, list);
    }

    private void appendList(ListVariableDescriptor<Solution_> variableDescriptor, Object entity, int fromIndex,
            int toIndex, List<Object> list) {
        var index = append(LIST);
        firsts[index] = variableDescriptor;
        seconds[index] = entity;
        thirds[index] = List.copyOf(list.subList(fromIndex, toIndex));
        fromIndexes[index] = fromIndex;
        sizes[index] = list.size();
        if (parent != null) {
            parent.appendList(variableDescriptor, entity, fromIndex, toIndex, list);
        }
    }

    private void openBracket(Object entity, int fromIndex, int toIndex, int listSize) {
        for (var i = 0; i < bracketCount; i++) {
            if (bracketEntities[i] == entity) {
                throw new IllegalArgumentException("""
                        The entity (%s) already has an open beforeListVariableChanged (%d, %d), \
                        so another beforeListVariableChanged (%d, %d) for it must not be opened.
                        Maybe close each beforeListVariableChanged with its afterListVariableChanged \
                        before changing the same entity again."""
                        .formatted(entity, bracketValues[i * 3], bracketValues[i * 3 + 1], fromIndex, toIndex));
            }
        }
        if (bracketCount == bracketEntities.length) {
            bracketEntities = Arrays.copyOf(bracketEntities, bracketCount * 2);
            bracketValues = Arrays.copyOf(bracketValues, bracketCount * 6);
        }
        bracketEntities[bracketCount] = entity;
        bracketValues[bracketCount * 3] = fromIndex;
        bracketValues[bracketCount * 3 + 1] = toIndex;
        bracketValues[bracketCount * 3 + 2] = listSize;
        bracketCount++;
    }

    /**
     * Call after a list variable changed; validates the call against its {@link #recordList} counterpart.
     *
     * @throws IllegalArgumentException if there is no counterpart, or the range does not match it
     */
    public void closeList(ListVariableDescriptor<Solution_> variableDescriptor, Object entity, int fromIndex,
            int toIndex) {
        if (muted) {
            return;
        }
        var i = 0;
        while (i < bracketCount && bracketEntities[i] != entity) {
            i++;
        }
        if (i == bracketCount) {
            throw new IllegalArgumentException("""
                    The afterListVariableChanged (%d, %d) of entity (%s) has no matching beforeListVariableChanged.
                    Maybe check implementation of your %s."""
                    .formatted(fromIndex, toIndex, entity, Move.class.getSimpleName()));
        }
        var requiredFromIndex = bracketValues[i * 3];
        if (requiredFromIndex != fromIndex) {
            throw new IllegalArgumentException("""
                    The fromIndex of afterListVariableChanged (%d) must match its beforeListVariableChanged counterpart (%d).
                    Maybe check implementation of your %s."""
                    .formatted(fromIndex, requiredFromIndex, Move.class.getSimpleName()));
        } else if (toIndex < fromIndex) {
            throw new IllegalArgumentException("""
                    The afterListVariableChanged (%d, %d) of entity (%s) has toIndex (%d) smaller than fromIndex (%d).
                    Maybe check implementation of your %s."""
                    .formatted(fromIndex, toIndex, entity, toIndex, fromIndex, Move.class.getSimpleName()));
        }
        var actualLengthDelta = variableDescriptor.getValue(entity).size() - bracketValues[i * 3 + 2];
        var reportedLengthDelta = toIndex - bracketValues[i * 3 + 1];
        if (actualLengthDelta != reportedLengthDelta) {
            throw new IllegalArgumentException("""
                    The afterListVariableChanged (%d, %d) of entity (%s) reports a length change of (%d), \
                    but its list variable actually changed length by (%d).
                    Maybe check implementation of your %s; \
                    its beforeListVariableChanged/afterListVariableChanged range must cover everything it changed."""
                    .formatted(fromIndex, toIndex, entity, reportedLengthDelta, actualLengthDelta,
                            Move.class.getSimpleName()));
        }
        bracketCount--;
        bracketEntities[i] = bracketEntities[bracketCount];
        System.arraycopy(bracketValues, bracketCount * 3, bracketValues, i * 3, 3);
        bracketEntities[bracketCount] = null;
    }

    public void recordBeforeElementAssigned(ListVariableDescriptor<Solution_> variableDescriptor, Object element) {
        recordElement(BEFORE_ASSIGNED, variableDescriptor, element);
    }

    public void recordAfterElementAssigned(ListVariableDescriptor<Solution_> variableDescriptor, Object element) {
        recordElement(AFTER_ASSIGNED, variableDescriptor, element);
    }

    public void recordBeforeElementUnassigned(ListVariableDescriptor<Solution_> variableDescriptor, Object element) {
        recordElement(BEFORE_UNASSIGNED, variableDescriptor, element);
    }

    public void recordAfterElementUnassigned(ListVariableDescriptor<Solution_> variableDescriptor, Object element) {
        recordElement(AFTER_UNASSIGNED, variableDescriptor, element);
    }

    private void recordElement(byte kind, ListVariableDescriptor<Solution_> variableDescriptor, Object element) {
        if (muted) {
            return;
        }
        var index = append(kind);
        firsts[index] = variableDescriptor;
        seconds[index] = element;
        if (parent != null) {
            parent.recordElement(kind, variableDescriptor, element);
        }
    }

    /**
     * Call before a supply changes its internal state; restore passes the same arguments to {@link UndoableSupply#undo}.
     * Does nothing in genuine-only mode.
     */
    public void recordSupplyChange(UndoableSupply supply, Object a, @Nullable Object b, int i) {
        if (muted) {
            return;
        }
        if (!genuineOnly) {
            var index = append(SUPPLY);
            firsts[index] = supply;
            seconds[index] = a;
            thirds[index] = b;
            fromIndexes[index] = i;
        }
        if (parent != null) {
            parent.recordSupplyChange(supply, a, b, i);
        }
    }

    private int append(byte kind) {
        if (size == kinds.length) {
            var capacity = size * 2;
            kinds = Arrays.copyOf(kinds, capacity);
            firsts = Arrays.copyOf(firsts, capacity);
            seconds = Arrays.copyOf(seconds, capacity);
            thirds = Arrays.copyOf(thirds, capacity);
            fromIndexes = Arrays.copyOf(fromIndexes, capacity);
            sizes = Arrays.copyOf(sizes, capacity);
        }
        kinds[size] = kind;
        return size++;
    }

    /**
     * Reverts all recorded changes in reverse order and clears the journal, which must already be closed.
     * The ancestors drop the records forwarded since {@link #begin}, as the state is back to that point.
     */
    public void restore(InnerScoreDirector<Solution_, ?> scoreDirector) {
        var currentParent = parent;
        if (currentParent == null) {
            restoreAndKeep(scoreDirector);
        } else {
            currentParent.muted = true;
            restoreAndKeep(scoreDirector);
            currentParent.muted = false;
            // Every ancestor got the same forwarded records.
            var forwardedCount = currentParent.size - parentMark;
            for (var ancestor = currentParent; ancestor != null; ancestor = ancestor.parent) {
                ancestor.truncate(ancestor.size - forwardedCount);
            }
        }
        clear();
    }

    /**
     * As {@link #restore}, but keeps the entries, so that the same changes can be reverted again later.
     */
    public void restoreAndKeep(InnerScoreDirector<Solution_, ?> scoreDirector) {
        if (genuineOnly) {
            replay(scoreDirector);
        } else {
            scoreDirector.setRestoring(true);
            for (var index = size - 1; index >= 0; index--) {
                restoreEntry(scoreDirector, index);
            }
            scoreDirector.setRestoring(false);
        }
        scoreDirector.assertUndoJournalRestored(this);
        if (hasCapturedState) {
            scoreDirector.restoreUndoState(variableSupportUndoState, lastVariableUpdateSuccessful);
        }
    }

    @SuppressWarnings("unchecked")
    private void restoreEntry(InnerScoreDirector<Solution_, ?> scoreDirector, int index) {
        switch (kinds[index]) {
            case VARIABLE -> {
                var variableDescriptor = (VariableDescriptor<Solution_>) firsts[index];
                var entity = seconds[index];
                scoreDirector.beforeVariableChanged(variableDescriptor, entity);
                variableDescriptor.setValue(entity, thirds[index]);
                scoreDirector.afterVariableChanged(variableDescriptor, entity);
            }
            case LIST -> restoreList(scoreDirector, index);
            case AFTER_ASSIGNED -> scoreDirector
                    .beforeListVariableElementUnassigned((ListVariableDescriptor<Solution_>) firsts[index], seconds[index]);
            case BEFORE_ASSIGNED -> scoreDirector
                    .afterListVariableElementUnassigned((ListVariableDescriptor<Solution_>) firsts[index], seconds[index]);
            case AFTER_UNASSIGNED -> scoreDirector
                    .beforeListVariableElementAssigned((ListVariableDescriptor<Solution_>) firsts[index], seconds[index]);
            case BEFORE_UNASSIGNED -> scoreDirector
                    .afterListVariableElementAssigned((ListVariableDescriptor<Solution_>) firsts[index], seconds[index]);
            case SUPPLY -> ((UndoableSupply) firsts[index]).undo(seconds[index], thirds[index], fromIndexes[index]);
            default -> throw new IllegalStateException("Impossible state: unknown entry kind (%d)."
                    .formatted(kinds[index]));
        }
    }

    @SuppressWarnings("unchecked")
    private void restoreList(InnerScoreDirector<Solution_, ?> scoreDirector, int index) {
        var variableDescriptor = (ListVariableDescriptor<Solution_>) firsts[index];
        var entity = seconds[index];
        var oldRange = (List<Object>) thirds[index];
        var fromIndex = fromIndexes[index];
        var list = variableDescriptor.getValue(entity);
        var newLength = list.size() - sizes[index] + oldRange.size();
        scoreDirector.beforeListVariableChanged(variableDescriptor, entity, fromIndex, fromIndex + newLength);
        list.subList(fromIndex, fromIndex + newLength).clear();
        list.addAll(fromIndex, oldRange);
        scoreDirector.afterListVariableChanged(variableDescriptor, entity, fromIndex, fromIndex + oldRange.size());
    }

    // Like a forward move: all events first, then one shadow variable update.
    private void replay(InnerScoreDirector<Solution_, ?> scoreDirector) {
        for (var index = size - 1; index >= 0; index--) {
            restoreEntry(scoreDirector, index);
        }
        scoreDirector.updateShadowVariables();
    }

    /**
     * The copy does not write back the state captured at {@link #begin}, as it restores at a later time.
     *
     * @return a detached copy, for an undo move that executes later
     */
    public UndoJournal<Solution_> copy() {
        var copy = new UndoJournal<Solution_>(null);
        var capacity = Math.max(size, 1);
        copy.kinds = Arrays.copyOf(kinds, capacity);
        copy.firsts = Arrays.copyOf(firsts, capacity);
        copy.seconds = Arrays.copyOf(seconds, capacity);
        copy.thirds = Arrays.copyOf(thirds, capacity);
        copy.fromIndexes = Arrays.copyOf(fromIndexes, capacity);
        copy.sizes = Arrays.copyOf(sizes, capacity);
        copy.size = size;
        copy.genuineOnly = genuineOnly;
        return copy;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    private void truncate(int newSize) {
        Arrays.fill(firsts, newSize, size, null);
        Arrays.fill(seconds, newSize, size, null);
        Arrays.fill(thirds, newSize, size, null);
        size = newSize;
    }

    public void clear() {
        truncate(0);
        Arrays.fill(bracketEntities, 0, bracketCount, null);
        bracketCount = 0;
        variableSupportUndoState = null;
    }

    @Override
    public String toString() {
        return "UndoJournal(%d entries%s)".formatted(size, genuineOnly ? ", genuine only" : "");
    }

}
