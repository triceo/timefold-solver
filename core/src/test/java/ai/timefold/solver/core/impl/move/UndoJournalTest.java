package ai.timefold.solver.core.impl.move;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.ArrayList;
import java.util.List;

import ai.timefold.solver.core.api.score.SimpleScore;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import ai.timefold.solver.core.impl.score.director.easy.EasyScoreDirectorFactory;
import ai.timefold.solver.core.preview.api.move.Move;
import ai.timefold.solver.core.testdomain.list.TestdataListEntity;
import ai.timefold.solver.core.testdomain.list.TestdataListSolution;
import ai.timefold.solver.core.testdomain.list.TestdataListValue;

import org.junit.jupiter.api.Test;

class UndoJournalTest {

    private static final ListVariableDescriptor<TestdataListSolution> LIST_VARIABLE =
            TestdataListSolution.buildSolutionDescriptor().getListVariableDescriptor();

    private final UndoJournal<TestdataListSolution> journal = newJournal();
    private final TestdataListEntity a = entity("a", "a0", "a1", "a2");
    private final TestdataListEntity b = entity("b", "b0", "b1");
    private final TestdataListEntity c = entity("c", "c0");

    private static UndoJournal<TestdataListSolution> newJournal() {
        var journal = new UndoJournal<TestdataListSolution>(null);
        journal.begin(false, null, true);
        return journal;
    }

    private static TestdataListEntity entity(String code, String... valueCodes) {
        var entity = new TestdataListEntity(code);
        entity.setValueList(new ArrayList<>(List.of(valueCodes).stream().map(TestdataListValue::new).toList()));
        return entity;
    }

    @Test
    void sequentialBracketsOnSameEntity() {
        assertThatCode(() -> {
            journal.recordList(LIST_VARIABLE, a, 0, 1);
            a.getValueList().removeFirst();
            journal.closeList(LIST_VARIABLE, a, 0, 0);
            journal.recordList(LIST_VARIABLE, a, 0, 1);
            journal.closeList(LIST_VARIABLE, a, 0, 1);
        }).doesNotThrowAnyException();
        assertThat(journal.size()).isEqualTo(2);
    }

    @Test
    void interleavedBracketsOnThreeEntities() {
        assertThatCode(() -> {
            journal.recordList(LIST_VARIABLE, a, 1, 2);
            journal.recordList(LIST_VARIABLE, b, 0, 1);
            journal.recordList(LIST_VARIABLE, c, 0, 1);
            journal.closeList(LIST_VARIABLE, b, 0, 1);
            journal.closeList(LIST_VARIABLE, a, 1, 2);
            journal.closeList(LIST_VARIABLE, c, 0, 1);
        }).doesNotThrowAnyException();
    }

    @Test
    void secondBracketForOpenEntityFails() {
        journal.recordList(LIST_VARIABLE, a, 0, 1);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> journal.recordList(LIST_VARIABLE, a, 1, 2))
                .withMessageContaining("already has an open beforeListVariableChanged (0, 1)");
    }

    @Test
    void afterWithoutBeforeFails() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> journal.closeList(LIST_VARIABLE, a, 0, 1))
                .withMessageContaining("has no matching beforeListVariableChanged");
    }

    @Test
    void duplicateAfterFails() {
        journal.recordList(LIST_VARIABLE, a, 0, 1);
        journal.closeList(LIST_VARIABLE, a, 0, 1);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> journal.closeList(LIST_VARIABLE, a, 0, 1))
                .withMessageContaining("has no matching beforeListVariableChanged");
    }

    @Test
    void mismatchedFromIndexFails() {
        journal.recordList(LIST_VARIABLE, a, 0, 2);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> journal.closeList(LIST_VARIABLE, a, 1, 2))
                .withMessageContaining("must match its beforeListVariableChanged counterpart (0)");
    }

    @Test
    void toIndexBelowFromIndexFails() {
        journal.recordList(LIST_VARIABLE, a, 2, 3);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> journal.closeList(LIST_VARIABLE, a, 2, 1))
                .withMessageContaining("smaller than fromIndex");
    }

    @Test
    void wrongLengthChangeFails() {
        journal.recordList(LIST_VARIABLE, a, 0, 3);
        a.getValueList().removeLast();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> journal.closeList(LIST_VARIABLE, a, 0, 3))
                .withMessageContaining("reports a length change of (0), but its list variable actually changed length by (-1)");
    }

    @Test
    void temporaryMoveWithWrongRangeFails() {
        var v0 = new TestdataListValue("v0");
        var v1 = new TestdataListValue("v1");
        var entity = new TestdataListEntity("e", v0, v1);
        var solution = new TestdataListSolution();
        solution.setEntityList(List.of(entity));
        solution.setValueList(List.of(v0, v1));
        SolutionManager.updateShadowVariables(solution);
        var scoreDirector = new EasyScoreDirectorFactory<TestdataListSolution, SimpleScore>(
                TestdataListSolution.buildSolutionDescriptor(), s -> SimpleScore.ZERO, EnvironmentMode.PHASE_ASSERT)
                .createScoreDirectorBuilder(EnvironmentMode.PHASE_ASSERT)
                .build();
        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        Move<TestdataListSolution> move = view -> {
            var innerScoreDirector = ((InnerMutableSolutionView<TestdataListSolution>) view).getScoreDirector();
            innerScoreDirector.beforeListVariableChanged(LIST_VARIABLE, entity, 0, 1);
            entity.getValueList().addFirst(v1);
            innerScoreDirector.afterListVariableChanged(LIST_VARIABLE, entity, 0, 1);
        };

        assertThatIllegalArgumentException()
                .isThrownBy(() -> scoreDirector.getMoveDirector().executeTemporary(move, score -> score))
                .withMessageContaining("reports a length change of (0)");
    }

}
