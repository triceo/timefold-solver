package ai.timefold.solver.core.impl.score.director.stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ai.timefold.solver.core.api.score.SimpleScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.config.score.director.ScoreDirectorFactoryConfig;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.impl.score.director.AbstractScoreDirector;
import ai.timefold.solver.core.impl.score.director.ScoreDirectorFactoryFactory;
import ai.timefold.solver.core.testdomain.TestdataConstraintProvider;
import ai.timefold.solver.core.testdomain.TestdataEntity;
import ai.timefold.solver.core.testdomain.TestdataSolution;
import ai.timefold.solver.core.testdomain.TestdataValue;
import ai.timefold.solver.core.testdomain.shadow.inverserelation.TestdataInverseRelationConstraintProvider;
import ai.timefold.solver.core.testdomain.shadow.inverserelation.TestdataInverseRelationEntity;
import ai.timefold.solver.core.testdomain.shadow.inverserelation.TestdataInverseRelationSolution;
import ai.timefold.solver.core.testdomain.shadow.inverserelation.TestdataInverseRelationValue;

import org.junit.jupiter.api.Test;

/**
 * Consecutive updates of one fact reach the session as one; the constraint penalizes each entity with a value.
 */
class BavetSessionUpdateCombiningTest {

    private static AbstractScoreDirector<TestdataSolution, SimpleScore, ?> buildScoreDirector(TestdataSolution solution) {
        var scoreDirectorFactory = new ScoreDirectorFactoryFactory<TestdataSolution, SimpleScore>(
                new ScoreDirectorFactoryConfig().withConstraintProviderClass(TestdataConstraintProvider.class))
                .buildScoreDirectorFactory(EnvironmentMode.PHASE_ASSERT, TestdataSolution.buildSolutionDescriptor());
        var scoreDirector = scoreDirectorFactory.buildScoreDirector();
        scoreDirector.setWorkingSolution(solution);
        assertThat(scoreDirector.calculateScore().raw()).isEqualTo(SimpleScore.of(-solution.getEntityList().size()));
        return scoreDirector;
    }

    private static void changeValue(AbstractScoreDirector<TestdataSolution, SimpleScore, ?> scoreDirector,
            TestdataEntity entity, TestdataValue value) {
        scoreDirector.beforeVariableChanged(entity, "value");
        entity.setValue(value);
        scoreDirector.afterVariableChanged(entity, "value");
    }

    @Test
    void filterSeesTheLastOfConsecutiveWrites() {
        var solution = TestdataSolution.generateSolution(2, 3);
        try (var scoreDirector = buildScoreDirector(solution)) {
            var entity = solution.getEntityList().get(0);
            var value = entity.getValue();
            // The forEach filter fails after the first write and passes after the second.
            changeValue(scoreDirector, entity, null);
            changeValue(scoreDirector, entity, value);
            changeValue(scoreDirector, solution.getEntityList().get(1), null);
            assertThat(scoreDirector.calculateScore().raw()).isEqualTo(SimpleScore.of(-2));
        }
    }

    @Test
    void pendingUpdateIsSentBeforeRetract() {
        var solution = TestdataSolution.generateSolution(2, 3);
        try (var scoreDirector = buildScoreDirector(solution)) {
            var entity = solution.getEntityList().get(0);
            changeValue(scoreDirector, entity, solution.getValueList().get(1));
            scoreDirector.beforeEntityRemoved(entity);
            solution.getEntityList().remove(entity);
            scoreDirector.afterEntityRemoved(entity);
            assertThat(scoreDirector.calculateScore().raw()).isEqualTo(SimpleScore.of(-2));
        }
    }

    @Test
    void scoreIsCorrectWhenInsertFollowsPendingUpdate() {
        var solution = TestdataSolution.generateSolution(2, 3);
        try (var scoreDirector = buildScoreDirector(solution)) {
            changeValue(scoreDirector, solution.getEntityList().get(0), null);
            var newEntity = new TestdataEntity("new", solution.getValueList().get(0));
            scoreDirector.beforeEntityAdded(newEntity);
            solution.getEntityList().add(newEntity);
            scoreDirector.afterEntityAdded(newEntity);
            assertThat(scoreDirector.calculateScore().raw()).isEqualTo(SimpleScore.of(-3));
        }
    }

    @Test
    void scoreIsCorrectWhenInverseShadowIsWrittenBetweenUpdates() {
        // Moving an entity to another value writes the old value's inverse, the entity and the new value's inverse.
        var v1 = new TestdataInverseRelationValue("v1");
        var v2 = new TestdataInverseRelationValue("v2");
        var e1 = new TestdataInverseRelationEntity("e1", v1);
        var e2 = new TestdataInverseRelationEntity("e2", v1);
        var e3 = new TestdataInverseRelationEntity("e3", v2);
        var solution = new TestdataInverseRelationSolution("s");
        solution.setValueList(new ArrayList<>(List.of(v1, v2)));
        solution.setEntityList(new ArrayList<>(List.of(e1, e2, e3)));
        var scoreDirectorFactory = new ScoreDirectorFactoryFactory<TestdataInverseRelationSolution, SimpleScore>(
                new ScoreDirectorFactoryConfig().withConstraintProviderClass(TestdataInverseRelationConstraintProvider.class))
                .buildScoreDirectorFactory(EnvironmentMode.PHASE_ASSERT,
                        TestdataInverseRelationSolution.buildSolutionDescriptor());
        try (var scoreDirector = scoreDirectorFactory.buildScoreDirector()) {
            scoreDirector.setWorkingSolution(solution);
            assertThat(scoreDirector.calculateScore().raw()).isEqualTo(SimpleScore.of(-5)); // 2² + 1²
            scoreDirector.beforeVariableChanged(e3, "value");
            e3.setValue(v1);
            scoreDirector.afterVariableChanged(e3, "value");
            assertThat(scoreDirector.calculateScore().raw()).isEqualTo(SimpleScore.of(-9)); // 3²
        }
    }

    @Test
    void updateOfUninsertedFactFailsAtOnceInStepAssert() {
        try (var scoreDirector = buildValueScoreDirector(EnvironmentMode.STEP_ASSERT)) {
            var session = Objects.requireNonNull(scoreDirector.getSession());
            var uninsertedValue = new TestdataValue("uninserted");
            assertThatThrownBy(() -> session.update(uninsertedValue))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("never inserted");
        }
    }

    @Test
    void updateOfUninsertedFactFailsAtNextSettleBelowStepAssert() {
        try (var scoreDirector = buildValueScoreDirector(EnvironmentMode.PHASE_ASSERT)) {
            var session = Objects.requireNonNull(scoreDirector.getSession());
            var uninsertedValue = new TestdataValue("uninserted");
            assertThatCode(() -> session.update(uninsertedValue)).doesNotThrowAnyException();
            assertThatThrownBy(session::settle)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("never inserted")
                    .hasMessageContaining(EnvironmentMode.STEP_ASSERT.name());
        }
    }

    @SuppressWarnings("unchecked")
    private static BavetConstraintStreamScoreDirector<TestdataSolution, SimpleScore>
            buildValueScoreDirector(EnvironmentMode environmentMode) {
        var scoreDirectorFactory = new ScoreDirectorFactoryFactory<TestdataSolution, SimpleScore>(
                new ScoreDirectorFactoryConfig().withConstraintProviderClass(TestdataValueConstraintProvider.class))
                .buildScoreDirectorFactory(environmentMode, TestdataSolution.buildSolutionDescriptor());
        var scoreDirector =
                (BavetConstraintStreamScoreDirector<TestdataSolution, SimpleScore>) scoreDirectorFactory.buildScoreDirector();
        scoreDirector.setWorkingSolution(TestdataSolution.generateSolution(2, 3));
        return scoreDirector;
    }

    // A problem fact goes through an unfiltered forEach node, which fails on an update of a fact it does not know.
    public static final class TestdataValueConstraintProvider implements ConstraintProvider {

        @Override
        public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
            return new Constraint[] {
                    constraintFactory.forEach(TestdataValue.class)
                            .penalize(SimpleScore.ONE)
                            .asConstraint("Penalize each value")
            };
        }

    }

}
