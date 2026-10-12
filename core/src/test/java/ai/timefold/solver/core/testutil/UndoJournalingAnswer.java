package ai.timefold.solver.core.testutil;

import ai.timefold.solver.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import ai.timefold.solver.core.impl.domain.variable.descriptor.VariableDescriptor;
import ai.timefold.solver.core.impl.move.UndoJournal;
import ai.timefold.solver.core.impl.score.director.InnerScoreDirector;

import org.jspecify.annotations.Nullable;
import org.mockito.Answers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Default answer for a mocked {@link InnerScoreDirector}, so that temporary moves on the mock are undone.
 * It keeps the open undo journals and records the variable events into them.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public final class UndoJournalingAnswer implements Answer<Object> {

    private @Nullable UndoJournal journal;

    @Override
    public @Nullable Object answer(InvocationOnMock invocation) throws Throwable {
        switch (invocation.getMethod().getName()) {
            case "openUndoJournal" -> {
                var newJournal = new UndoJournal(journal);
                newJournal.begin(false, null, true);
                journal = newJournal;
                return newJournal;
            }
            case "closeUndoJournal" -> {
                journal = journal.getParent();
                return null;
            }
            case "getUndoJournal" -> {
                return journal;
            }
            case "beforeVariableChanged" -> {
                if (journal != null) {
                    journal.recordVariable((VariableDescriptor) invocation.getArgument(0), invocation.getArgument(1));
                }
                return null;
            }
            case "beforeListVariableChanged" -> {
                if (journal != null) {
                    journal.recordList((ListVariableDescriptor) invocation.getArgument(0), invocation.getArgument(1),
                            invocation.<Integer> getArgument(2), invocation.<Integer> getArgument(3));
                }
                return null;
            }
            case "beforeListVariableElementAssigned" -> {
                if (journal != null) {
                    journal.recordBeforeElementAssigned((ListVariableDescriptor) invocation.getArgument(0),
                            invocation.getArgument(1));
                }
                return null;
            }
            case "beforeListVariableElementUnassigned" -> {
                if (journal != null) {
                    journal.recordBeforeElementUnassigned((ListVariableDescriptor) invocation.getArgument(0),
                            invocation.getArgument(1));
                }
                return null;
            }
            case "afterListVariableChanged" -> {
                if (journal != null) {
                    journal.closeList((ListVariableDescriptor) invocation.getArgument(0), invocation.getArgument(1),
                            invocation.<Integer> getArgument(2), invocation.<Integer> getArgument(3));
                }
                return null;
            }
            case "afterListVariableElementAssigned" -> {
                if (journal != null) {
                    journal.recordAfterElementAssigned((ListVariableDescriptor) invocation.getArgument(0),
                            invocation.getArgument(1));
                }
                return null;
            }
            case "afterListVariableElementUnassigned" -> {
                if (journal != null) {
                    journal.recordAfterElementUnassigned((ListVariableDescriptor) invocation.getArgument(0),
                            invocation.getArgument(1));
                }
                return null;
            }
            default -> {
                return Answers.RETURNS_DEFAULTS.answer(invocation);
            }
        }
    }

}
