package ai.timefold.solver.test.api.score.stream;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.math.BigDecimal;
import java.util.Collections;

import ai.timefold.solver.core.api.score.buildin.simple.SimpleScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.impl.testdata.domain.TestdataValue;
import ai.timefold.solver.core.impl.testdata.domain.list.TestdataListEntity;
import ai.timefold.solver.core.impl.testdata.domain.list.TestdataListSolution;
import ai.timefold.solver.core.impl.testdata.domain.list.TestdataListValue;
import ai.timefold.solver.core.impl.testdata.domain.list.allows_unassigned_values.TestdataAllowsUnassignedValuesListEntity;
import ai.timefold.solver.core.impl.testdata.domain.list.allows_unassigned_values.TestdataAllowsUnassignedValuesListSolution;
import ai.timefold.solver.core.impl.testdata.domain.list.allows_unassigned_values.TestdataAllowsUnassignedValuesListValue;
import ai.timefold.solver.core.impl.testdata.domain.list.pinned.TestdataPinnedListEntity;
import ai.timefold.solver.core.impl.testdata.domain.list.pinned.TestdataPinnedListSolution;
import ai.timefold.solver.core.impl.testdata.domain.list.pinned.TestdataPinnedListValue;
import ai.timefold.solver.test.api.score.stream.testdata.TestdataConstraintVerifierConstraintProvider;
import ai.timefold.solver.test.api.score.stream.testdata.TestdataConstraintVerifierExtendedSolution;
import ai.timefold.solver.test.api.score.stream.testdata.TestdataConstraintVerifierFirstEntity;
import ai.timefold.solver.test.api.score.stream.testdata.TestdataConstraintVerifierSecondEntity;
import ai.timefold.solver.test.api.score.stream.testdata.TestdataConstraintVerifierSolution;

import org.junit.jupiter.api.Test;

class SingleConstraintAssertionTest {

    private final ConstraintVerifier<TestdataConstraintVerifierConstraintProvider, TestdataConstraintVerifierExtendedSolution> constraintVerifier =
            ConstraintVerifier.build(new TestdataConstraintVerifierConstraintProvider(),
                    TestdataConstraintVerifierExtendedSolution.class,
                    TestdataConstraintVerifierFirstEntity.class,
                    TestdataConstraintVerifierSecondEntity.class);

    @Test
    void penalizesAndDoesNotReward() {
        TestdataConstraintVerifierSolution solution = TestdataConstraintVerifierSolution.generateSolution(2, 3);

        assertThatCode(() -> constraintVerifier.verifyThat(TestdataConstraintVerifierConstraintProvider::penalizeEveryEntity)
                .given(solution.getEntityList().toArray())
                .penalizes("There should be penalties.")).doesNotThrowAnyException();
        assertThatCode(() -> constraintVerifier.verifyThat(TestdataConstraintVerifierConstraintProvider::penalizeEveryEntity)
                .given(solution.getEntityList().toArray())
                .rewards("There should be rewards")).hasMessageContaining("There should be rewards")
                .hasMessageContaining("Expected reward");
    }

    @Test
    void rewardsButDoesNotPenalize() {
        TestdataConstraintVerifierSolution solution = TestdataConstraintVerifierSolution.generateSolution(2, 3);

        assertThatCode(() -> constraintVerifier.verifyThat(TestdataConstraintVerifierConstraintProvider::rewardEveryEntity)
                .given(solution.getEntityList().toArray())
                .rewards("There should be rewards")).doesNotThrowAnyException();
        assertThatCode(() -> constraintVerifier.verifyThat(TestdataConstraintVerifierConstraintProvider::rewardEveryEntity)
                .given(solution.getEntityList().toArray())
                .penalizes("There should be penalties.")).hasMessageContaining("There should be penalties")
                .hasMessageContaining("Expected penalty");
    }

    @Test
    void impacts() {
        assertThatCode(() -> constraintVerifier.verifyThat(TestdataConstraintVerifierConstraintProvider::impactEveryEntity)
                .given()
                .penalizes(0, "There should be no penalties"))
                .doesNotThrowAnyException();
        assertThatCode(() -> constraintVerifier.verifyThat(TestdataConstraintVerifierConstraintProvider::impactEveryEntity)
                .given(new TestdataConstraintVerifierFirstEntity("A", new TestdataValue()))
                .penalizes(0, "There should be no penalties"))
                .hasMessageContaining("There should be no penalties")
                .hasMessageContaining("Expected penalty");
        assertThatCode(() -> constraintVerifier.verifyThat(TestdataConstraintVerifierConstraintProvider::impactEveryEntity)
                .given(new TestdataConstraintVerifierFirstEntity("A", new TestdataValue()))
                .penalizes(1, "There should be penalties"))
                .doesNotThrowAnyException();
        assertThatCode(() -> constraintVerifier.verifyThat(TestdataConstraintVerifierConstraintProvider::impactEveryEntity)
                .given(new TestdataConstraintVerifierFirstEntity("A", new TestdataValue()))
                .penalizes(2, "There should only be one penalty"))
                .hasMessageContaining("There should only be one penalty")
                .hasMessageContaining("Expected penalty");
        assertThatCode(() -> constraintVerifier.verifyThat(TestdataConstraintVerifierConstraintProvider::impactEveryEntity)
                .given(new TestdataConstraintVerifierFirstEntity("A", new TestdataValue()))
                .rewards(1, "There should not be rewards"))
                .hasMessageContaining("There should not be rewards")
                .hasMessageContaining("Expected reward");

        assertThatCode(() -> constraintVerifier.verifyThat(TestdataConstraintVerifierConstraintProvider::impactEveryEntity)
                .given()
                .rewards(0, "There should be no rewards"))
                .doesNotThrowAnyException();
        assertThatCode(() -> constraintVerifier.verifyThat(TestdataConstraintVerifierConstraintProvider::impactEveryEntity)
                .given(new TestdataConstraintVerifierFirstEntity("B", new TestdataValue()))
                .rewards(0, "There should be no rewards"))
                .hasMessageContaining("There should be no rewards")
                .hasMessageContaining("Expected reward");
        assertThatCode(() -> constraintVerifier.verifyThat(TestdataConstraintVerifierConstraintProvider::impactEveryEntity)
                .given(new TestdataConstraintVerifierFirstEntity("B", new TestdataValue()))
                .rewards(1, "There should be rewards"))
                .doesNotThrowAnyException();
        assertThatCode(() -> constraintVerifier.verifyThat(TestdataConstraintVerifierConstraintProvider::impactEveryEntity)
                .given(new TestdataConstraintVerifierFirstEntity("B", new TestdataValue()))
                .rewards(2, "There should only be one reward"))
                .hasMessageContaining("There should only be one reward")
                .hasMessageContaining("Expected reward");
        assertThatCode(() -> constraintVerifier.verifyThat(TestdataConstraintVerifierConstraintProvider::impactEveryEntity)
                .given(new TestdataConstraintVerifierFirstEntity("B", new TestdataValue()))
                .penalizes(1, "There should not be penalties"))
                .hasMessageContaining("There should not be penalties")
                .hasMessageContaining("Expected penalty");
    }

    @Test
    void impactsBy() {
        assertThatCode(() -> constraintVerifier.verifyThat(TestdataConstraintVerifierConstraintProvider::impactEveryEntity)
                .given()
                .penalizesBy(0, "There should no penalties"))
                .doesNotThrowAnyException();
        assertThatCode(() -> constraintVerifier.verifyThat(TestdataConstraintVerifierConstraintProvider::impactEveryEntity)
                .given(new TestdataConstraintVerifierFirstEntity("A", new TestdataValue()))
                .penalizesBy(0, "There should be no penalties"))
                .hasMessageContaining("There should be no penalties")
                .hasMessageContaining("Expected penalty");
        assertThatCode(() -> constraintVerifier.verifyThat(TestdataConstraintVerifierConstraintProvider::impactEveryEntity)
                .given(new TestdataConstraintVerifierFirstEntity("A", new TestdataValue()))
                .penalizesBy(1, "There should be penalties"))
                .doesNotThrowAnyException();
        assertThatCode(() -> constraintVerifier.verifyThat(TestdataConstraintVerifierConstraintProvider::impactEveryEntity)
                .given(new TestdataConstraintVerifierFirstEntity("A", new TestdataValue()))
                .penalizesBy(2, "There should only be one penalty"))
                .hasMessageContaining("There should only be one penalty")
                .hasMessageContaining("Expected penalty");
        assertThatCode(() -> constraintVerifier.verifyThat(TestdataConstraintVerifierConstraintProvider::impactEveryEntity)
                .given(new TestdataConstraintVerifierFirstEntity("A", new TestdataValue()))
                .rewardsWith(1, "There should not be rewards"))
                .hasMessageContaining("There should not be rewards")
                .hasMessageContaining("Expected reward");

        assertThatCode(() -> constraintVerifier.verifyThat(TestdataConstraintVerifierConstraintProvider::impactEveryEntity)
                .given()
                .rewardsWith(0, "There should no rewards"))
                .doesNotThrowAnyException();
        assertThatCode(() -> constraintVerifier.verifyThat(TestdataConstraintVerifierConstraintProvider::impactEveryEntity)
                .given(new TestdataConstraintVerifierFirstEntity("B", new TestdataValue()))
                .rewardsWith(0, "There should be no rewards"))
                .hasMessageContaining("There should be no rewards")
                .hasMessageContaining("Expected reward");
        assertThatCode(() -> constraintVerifier.verifyThat(TestdataConstraintVerifierConstraintProvider::impactEveryEntity)
                .given(new TestdataConstraintVerifierFirstEntity("B", new TestdataValue()))
                .rewardsWith(1, "There should be rewards"))
                .doesNotThrowAnyException();
        assertThatCode(() -> constraintVerifier.verifyThat(TestdataConstraintVerifierConstraintProvider::impactEveryEntity)
                .given(new TestdataConstraintVerifierFirstEntity("B", new TestdataValue()))
                .rewardsWith(2, "There should only be one reward"))
                .hasMessageContaining("There should only be one reward")
                .hasMessageContaining("Expected reward");
        assertThatCode(() -> constraintVerifier.verifyThat(TestdataConstraintVerifierConstraintProvider::impactEveryEntity)
                .given(new TestdataConstraintVerifierFirstEntity("B", new TestdataValue()))
                .penalizesBy(1, "There should not be penalties"))
                .hasMessageContaining("There should not be penalties")
                .hasMessageContaining("Expected penalty");
    }

    @Test
    void penalizesByCountAndDoesNotReward() {
        TestdataConstraintVerifierSolution solution = TestdataConstraintVerifierSolution.generateSolution(2, 3);

        assertThatCode(() -> constraintVerifier.verifyThat(TestdataConstraintVerifierConstraintProvider::penalizeEveryEntity)
                .given(solution.getEntityList().toArray())
                .penalizes(3, "There should be penalties.")).doesNotThrowAnyException();
        assertThatCode(() -> constraintVerifier.verifyThat(TestdataConstraintVerifierConstraintProvider::penalizeEveryEntity)
                .given(solution.getEntityList().toArray())
                .rewards(1, "There should be rewards")).hasMessageContaining("There should be rewards")
                .hasMessageContaining("Expected reward");
    }

    @Test
    void penalizesByBigDecimal() {
        TestdataConstraintVerifierSolution solution = TestdataConstraintVerifierSolution.generateSolution(2, 3);

        assertThatCode(() -> constraintVerifier.verifyThat(TestdataConstraintVerifierConstraintProvider::penalizeEveryEntity)
                .given(solution.getEntityList().toArray())
                .penalizesBy(BigDecimal.valueOf(3), "There should be penalties.")).doesNotThrowAnyException();
        assertThatCode(() -> constraintVerifier.verifyThat(TestdataConstraintVerifierConstraintProvider::penalizeEveryEntity)
                .given(solution.getEntityList().toArray())
                .penalizesBy(new BigDecimal("3.01"), "There should be penalties."))
                .hasMessageContaining("There should be penalties")
                .hasMessageContaining("Expected penalty");
    }

    @Test
    void rewardsByCountButDoesNotPenalize() {
        TestdataConstraintVerifierSolution solution = TestdataConstraintVerifierSolution.generateSolution(2, 3);

        assertThatCode(() -> constraintVerifier.verifyThat(TestdataConstraintVerifierConstraintProvider::rewardEveryEntity)
                .given(solution.getEntityList().toArray())
                .rewards(3, "There should be rewards")).doesNotThrowAnyException();
        assertThatCode(() -> constraintVerifier.verifyThat(TestdataConstraintVerifierConstraintProvider::rewardEveryEntity)
                .given(solution.getEntityList().toArray())
                .penalizes(1, "There should be penalties.")).hasMessageContaining("There should be penalties")
                .hasMessageContaining("Expected penalty");
    }

    @Test
    void rewardsByBigDecimal() {
        TestdataConstraintVerifierSolution solution = TestdataConstraintVerifierSolution.generateSolution(2, 3);

        assertThatCode(() -> constraintVerifier.verifyThat(TestdataConstraintVerifierConstraintProvider::rewardEveryEntity)
                .given(solution.getEntityList().toArray())
                .rewardsWith(BigDecimal.valueOf(3), "There should be rewards")).doesNotThrowAnyException();
        assertThatCode(() -> constraintVerifier.verifyThat(TestdataConstraintVerifierConstraintProvider::rewardEveryEntity)
                .given(solution.getEntityList().toArray())
                .rewardsWith(new BigDecimal("3.01"), "There should be rewards."))
                .hasMessageContaining("There should be rewards")
                .hasMessageContaining("Expected reward");
    }

    @Test
    void uniquePairShouldWorkOnStringPlanningId() {
        assertThatCode(() -> constraintVerifier
                .verifyThat(TestdataConstraintVerifierConstraintProvider::differentStringEntityHaveDifferentValues)
                .given(new TestdataConstraintVerifierSecondEntity("A", "1"),
                        new TestdataConstraintVerifierSecondEntity("B", "1"))
                .penalizes(1, "There should be penalties")).doesNotThrowAnyException();

        assertThatCode(() -> constraintVerifier
                .verifyThat(TestdataConstraintVerifierConstraintProvider::differentStringEntityHaveDifferentValues)
                .given(new TestdataConstraintVerifierSecondEntity("A", "1"),
                        new TestdataConstraintVerifierSecondEntity("B", "1"))
                .rewards(1, "There should be rewards")).hasMessageContaining("There should be rewards")
                .hasMessageContaining("Expected reward");
    }

    @Test
    void listVarUnassignedWhileAllowsUnassigned() {
        var constraintVerifier =
                ConstraintVerifier.build(new TestdataAllowsUnassignedListConstraintProvider(),
                        TestdataAllowsUnassignedValuesListSolution.class,
                        TestdataAllowsUnassignedValuesListEntity.class,
                        TestdataAllowsUnassignedValuesListValue.class);

        var value1 = new TestdataAllowsUnassignedValuesListValue("v1");
        var value2 = new TestdataAllowsUnassignedValuesListValue("v2");
        var entity = new TestdataAllowsUnassignedValuesListEntity("eA");
        entity.setValueList(Collections.singletonList(value1));
        value1.setIndex(0);
        value1.setEntity(entity);

        assertThatCode(() -> constraintVerifier
                .verifyThat(TestdataAllowsUnassignedListConstraintProvider::penalizeEveryAssignedValue)
                .given(entity, value1, value2)
                .penalizes(1, "There should be penalties")).doesNotThrowAnyException();
        assertThatCode(() -> constraintVerifier
                .verifyThat(TestdataAllowsUnassignedListConstraintProvider::penalizeEveryValue)
                .given(entity, value1, value2)
                .penalizes(2, "There should be penalties")).doesNotThrowAnyException();
    }

    private static final class TestdataAllowsUnassignedListConstraintProvider implements ConstraintProvider {

        @Override
        public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
            return new Constraint[] {
                    penalizeEveryAssignedValue(constraintFactory),
                    penalizeEveryValue(constraintFactory)
            };
        }

        private Constraint penalizeEveryAssignedValue(ConstraintFactory constraintFactory) {
            return constraintFactory.forEach(TestdataAllowsUnassignedValuesListValue.class)
                    .penalize(SimpleScore.ONE)
                    .asConstraint("Penalize every unassigned value");
        }

        private Constraint penalizeEveryValue(ConstraintFactory constraintFactory) {
            return constraintFactory.forEachIncludingUnassigned(TestdataAllowsUnassignedValuesListValue.class)
                    .penalize(SimpleScore.ONE)
                    .asConstraint("Penalize every value");
        }

    }

    @Test
    void listVarUnassignedWhileDisallowsUnassigned() {
        var constraintVerifier =
                ConstraintVerifier.build(new TestdataDisallowsUnassignedListConstraintProvider(),
                        TestdataListSolution.class,
                        TestdataListEntity.class,
                        TestdataListValue.class);

        var value1 = new TestdataListValue("v1");
        var value2 = new TestdataListValue("v2");
        var entity = new TestdataListEntity("eA", value1);
        value1.setIndex(0);
        value1.setEntity(entity);

        assertThatCode(() -> constraintVerifier
                .verifyThat(TestdataDisallowsUnassignedListConstraintProvider::penalizeEveryAssignedValue)
                .given(entity, value1, value2)
                .penalizes(1, "There should be penalties")).doesNotThrowAnyException();
        assertThatCode(() -> constraintVerifier
                .verifyThat(TestdataDisallowsUnassignedListConstraintProvider::penalizeEveryValue)
                .given(entity, value1, value2)
                .penalizes(2, "There should be penalties")).doesNotThrowAnyException();
    }

    private static final class TestdataDisallowsUnassignedListConstraintProvider implements ConstraintProvider {

        @Override
        public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
            return new Constraint[] {
                    penalizeEveryAssignedValue(constraintFactory),
                    penalizeEveryValue(constraintFactory)
            };
        }

        private Constraint penalizeEveryAssignedValue(ConstraintFactory constraintFactory) {
            return constraintFactory.forEach(TestdataListValue.class)
                    .penalize(SimpleScore.ONE)
                    .asConstraint("Penalize every unassigned value");
        }

        private Constraint penalizeEveryValue(ConstraintFactory constraintFactory) {
            return constraintFactory.forEachIncludingUnassigned(TestdataListValue.class)
                    .penalize(SimpleScore.ONE)
                    .asConstraint("Penalize every value");
        }

    }

    @Test
    void listVarUnassignedWhileDisallowsUnassigned_noInverseRelationShadowVar() {
        var constraintVerifier =
                ConstraintVerifier.build(new TestdataDisallowsUnassignedListWithoutInverseShadowVarConstraintProvider(),
                        TestdataPinnedListSolution.class,
                        TestdataPinnedListEntity.class,
                        TestdataPinnedListValue.class);

        var value1 = new TestdataPinnedListValue("v1");
        var value2 = new TestdataPinnedListValue("v2");
        var entity = new TestdataPinnedListEntity("eA", value1);
        value1.setIndex(0);

        assertThatCode(() -> constraintVerifier
                .verifyThat(
                        TestdataDisallowsUnassignedListWithoutInverseShadowVarConstraintProvider::penalizeEveryAssignedValue)
                .given(entity, value1, value2)
                .penalizes(1, "There should be penalties")).doesNotThrowAnyException();
        assertThatCode(() -> constraintVerifier
                .verifyThat(TestdataDisallowsUnassignedListWithoutInverseShadowVarConstraintProvider::penalizeEveryValue)
                .given(entity, value1, value2)
                .penalizes(2, "There should be penalties")).doesNotThrowAnyException();
    }

    private static final class TestdataDisallowsUnassignedListWithoutInverseShadowVarConstraintProvider
            implements ConstraintProvider {

        @Override
        public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
            return new Constraint[] {
                    penalizeEveryAssignedValue(constraintFactory),
                    penalizeEveryValue(constraintFactory)
            };
        }

        private Constraint penalizeEveryAssignedValue(ConstraintFactory constraintFactory) {
            return constraintFactory.forEach(TestdataPinnedListValue.class)
                    .penalize(SimpleScore.ONE)
                    .asConstraint("Penalize every unassigned value");
        }

        private Constraint penalizeEveryValue(ConstraintFactory constraintFactory) {
            return constraintFactory.forEachIncludingUnassigned(TestdataPinnedListValue.class)
                    .penalize(SimpleScore.ONE)
                    .asConstraint("Penalize every value");
        }

    }

}
