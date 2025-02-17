package ai.timefold.solver.migration.v8;

import static org.openrewrite.java.Assertions.java;

import ai.timefold.solver.migration.AbstractRecipe;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

@Execution(ExecutionMode.CONCURRENT)
class AsConstraintRecipeTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new AsConstraintRecipe())
                .parser(AbstractRecipe.JAVA_PARSER);
    }

    // ************************************************************************
    // Uni
    // ************************************************************************

    @Test
    void uniPenalizeName() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .penalize("My constraint", HardSoftScore.ONE_HARD);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .penalize()
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void uniPenalizeId() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .penalize("My package", "My constraint", HardSoftScore.ONE_HARD);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .penalize()
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void uniPenalizeNameMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .penalize("My constraint", HardSoftScore.ONE_HARD, (a) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .penalize()
                                        .withMatchWeight((a) -> 7)
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void uniPenalizeIdMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .penalize("My package", "My constraint", HardSoftScore.ONE_HARD, (a) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .penalize()
                                        .withMatchWeight((a) -> 7)
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void uniPenalizeNameMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .penalizeLong("My constraint", HardSoftLongScore.ONE_HARD, (a) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .penalize()
                                        .withLongMatchWeight((a) -> 7L)
                                        .usingDefaultConstraintWeight(HardSoftLongScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void uniPenalizeIdMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .penalizeLong("My package", "My constraint", HardSoftLongScore.ONE_HARD, (a) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .penalize()
                                        .withLongMatchWeight((a) -> 7L)
                                        .usingDefaultConstraintWeight(HardSoftLongScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void uniPenalizeNameMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .penalizeBigDecimal("My constraint", HardSoftBigDecimalScore.ONE_HARD, (a) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .penalize()
                                        .withBigDecimalMatchWeight((a) -> BigDecimal.TEN)
                                        .usingDefaultConstraintWeight(HardSoftBigDecimalScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void uniPenalizeIdMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .penalizeBigDecimal("My package", "My constraint", HardSoftBigDecimalScore.ONE_HARD, (a) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .penalize()
                                        .withBigDecimalMatchWeight((a) -> BigDecimal.TEN)
                                        .usingDefaultConstraintWeight(HardSoftBigDecimalScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void uniRewardName() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .reward("My constraint", HardSoftScore.ONE_HARD);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .reward()
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void uniRewardId() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .reward("My package", "My constraint", HardSoftScore.ONE_HARD);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .reward()
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void uniRewardNameMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .reward("My constraint", HardSoftScore.ONE_HARD, (a) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .reward()
                                        .withMatchWeight((a) -> 7)
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void uniRewardIdMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .reward("My package", "My constraint", HardSoftScore.ONE_HARD, (a) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .reward()
                                        .withMatchWeight((a) -> 7)
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void uniRewardNameMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .rewardLong("My constraint", HardSoftLongScore.ONE_HARD, (a) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .reward()
                                        .withLongMatchWeight((a) -> 7L)
                                        .usingDefaultConstraintWeight(HardSoftLongScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void uniRewardIdMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .rewardLong("My package", "My constraint", HardSoftLongScore.ONE_HARD, (a) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .reward()
                                        .withLongMatchWeight((a) -> 7L)
                                        .usingDefaultConstraintWeight(HardSoftLongScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void uniRewardNameMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .rewardBigDecimal("My constraint", HardSoftBigDecimalScore.ONE_HARD, (a) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .reward()
                                        .withBigDecimalMatchWeight((a) -> BigDecimal.TEN)
                                        .usingDefaultConstraintWeight(HardSoftBigDecimalScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void uniRewardIdMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .rewardBigDecimal("My package", "My constraint", HardSoftBigDecimalScore.ONE_HARD, (a) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .reward()
                                        .withBigDecimalMatchWeight((a) -> BigDecimal.TEN)
                                        .usingDefaultConstraintWeight(HardSoftBigDecimalScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void uniImpactName() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .impact("My constraint", HardSoftScore.ONE_HARD);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .impact()
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void uniImpactId() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .impact("My package", "My constraint", HardSoftScore.ONE_HARD);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .impact()
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void uniImpactNameMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .impact("My constraint", HardSoftScore.ONE_HARD, (a) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .impact()
                                        .withMatchWeight((a) -> 7)
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void uniImpactIdMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .impact("My package", "My constraint", HardSoftScore.ONE_HARD, (a) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .impact()
                                        .withMatchWeight((a) -> 7)
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void uniImpactNameMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .impactLong("My constraint", HardSoftLongScore.ONE_HARD, (a) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .impact()
                                        .withLongMatchWeight((a) -> 7L)
                                        .usingDefaultConstraintWeight(HardSoftLongScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void uniImpactIdMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .impactLong("My package", "My constraint", HardSoftLongScore.ONE_HARD, (a) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .impact()
                                        .withLongMatchWeight((a) -> 7L)
                                        .usingDefaultConstraintWeight(HardSoftLongScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void uniImpactNameMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .impactBigDecimal("My constraint", HardSoftBigDecimalScore.ONE_HARD, (a) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .impact()
                                        .withBigDecimalMatchWeight((a) -> BigDecimal.TEN)
                                        .usingDefaultConstraintWeight(HardSoftBigDecimalScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void uniImpactIdMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .impactBigDecimal("My package", "My constraint", HardSoftBigDecimalScore.ONE_HARD, (a) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .impact()
                                        .withBigDecimalMatchWeight((a) -> BigDecimal.TEN)
                                        .usingDefaultConstraintWeight(HardSoftBigDecimalScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    // ************************************************************************
    // Bi
    // ************************************************************************

    @Test
    void biPenalizeName() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalize("My constraint", HardSoftScore.ONE_HARD);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalize()
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void biPenalizeId() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalize("My package", "My constraint", HardSoftScore.ONE_HARD);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalize()
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void biPenalizeNameMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalize("My constraint", HardSoftScore.ONE_HARD, (a, b) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalize()
                                        .withMatchWeight((a, b) -> 7)
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void biPenalizeIdMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalize("My package", "My constraint", HardSoftScore.ONE_HARD, (a, b) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalize()
                                        .withMatchWeight((a, b) -> 7)
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void biPenalizeNameMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalizeLong("My constraint", HardSoftLongScore.ONE_HARD, (a, b) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalize()
                                        .withLongMatchWeight((a, b) -> 7L)
                                        .usingDefaultConstraintWeight(HardSoftLongScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void biPenalizeIdMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalizeLong("My package", "My constraint", HardSoftLongScore.ONE_HARD, (a, b) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalize()
                                        .withLongMatchWeight((a, b) -> 7L)
                                        .usingDefaultConstraintWeight(HardSoftLongScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void biPenalizeNameMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalizeBigDecimal("My constraint", HardSoftBigDecimalScore.ONE_HARD, (a, b) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalize()
                                        .withBigDecimalMatchWeight((a, b) -> BigDecimal.TEN)
                                        .usingDefaultConstraintWeight(HardSoftBigDecimalScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void biPenalizeIdMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalizeBigDecimal("My package", "My constraint", HardSoftBigDecimalScore.ONE_HARD, (a, b) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalize()
                                        .withBigDecimalMatchWeight((a, b) -> BigDecimal.TEN)
                                        .usingDefaultConstraintWeight(HardSoftBigDecimalScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void biRewardName() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .reward("My constraint", HardSoftScore.ONE_HARD);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .reward()
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void biRewardId() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .reward("My package", "My constraint", HardSoftScore.ONE_HARD);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .reward()
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void biRewardNameMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .reward("My constraint", HardSoftScore.ONE_HARD, (a, b) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .reward()
                                        .withMatchWeight((a, b) -> 7)
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void biRewardIdMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .reward("My package", "My constraint", HardSoftScore.ONE_HARD, (a, b) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .reward()
                                        .withMatchWeight((a, b) -> 7)
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void biRewardNameMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .rewardLong("My constraint", HardSoftLongScore.ONE_HARD, (a, b) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .reward()
                                        .withLongMatchWeight((a, b) -> 7L)
                                        .usingDefaultConstraintWeight(HardSoftLongScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void biRewardIdMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .rewardLong("My package", "My constraint", HardSoftLongScore.ONE_HARD, (a, b) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .reward()
                                        .withLongMatchWeight((a, b) -> 7L)
                                        .usingDefaultConstraintWeight(HardSoftLongScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void biRewardNameMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .rewardBigDecimal("My constraint", HardSoftBigDecimalScore.ONE_HARD, (a, b) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .reward()
                                        .withBigDecimalMatchWeight((a, b) -> BigDecimal.TEN)
                                        .usingDefaultConstraintWeight(HardSoftBigDecimalScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void biRewardIdMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .rewardBigDecimal("My package", "My constraint", HardSoftBigDecimalScore.ONE_HARD, (a, b) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .reward()
                                        .withBigDecimalMatchWeight((a, b) -> BigDecimal.TEN)
                                        .usingDefaultConstraintWeight(HardSoftBigDecimalScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void biImpactName() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .impact("My constraint", HardSoftScore.ONE_HARD);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .impact()
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void biImpactId() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .impact("My package", "My constraint", HardSoftScore.ONE_HARD);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .impact()
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void biImpactNameMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .impact("My constraint", HardSoftScore.ONE_HARD, (a, b) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .impact()
                                        .withMatchWeight((a, b) -> 7)
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void biImpactIdMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .impact("My package", "My constraint", HardSoftScore.ONE_HARD, (a, b) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .impact()
                                        .withMatchWeight((a, b) -> 7)
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void biImpactNameMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .impactLong("My constraint", HardSoftLongScore.ONE_HARD, (a, b) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .impact()
                                        .withLongMatchWeight((a, b) -> 7L)
                                        .usingDefaultConstraintWeight(HardSoftLongScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void biImpactIdMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .impactLong("My package", "My constraint", HardSoftLongScore.ONE_HARD, (a, b) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .impact()
                                        .withLongMatchWeight((a, b) -> 7L)
                                        .usingDefaultConstraintWeight(HardSoftLongScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void biImpactNameMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .impactBigDecimal("My constraint", HardSoftBigDecimalScore.ONE_HARD, (a, b) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .impact()
                                        .withBigDecimalMatchWeight((a, b) -> BigDecimal.TEN)
                                        .usingDefaultConstraintWeight(HardSoftBigDecimalScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void biImpactIdMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .impactBigDecimal("My package", "My constraint", HardSoftBigDecimalScore.ONE_HARD, (a, b) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .impact()
                                        .withBigDecimalMatchWeight((a, b) -> BigDecimal.TEN)
                                        .usingDefaultConstraintWeight(HardSoftBigDecimalScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    // ************************************************************************
    // Tri
    // ************************************************************************

    @Test
    void triPenalizeName() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalize("My constraint", HardSoftScore.ONE_HARD);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalize()
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void triPenalizeId() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalize("My package", "My constraint", HardSoftScore.ONE_HARD);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalize()
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void triPenalizeNameMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalize("My constraint", HardSoftScore.ONE_HARD, (a, b, c) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalize()
                                        .withMatchWeight((a, b, c) -> 7)
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void triPenalizeIdMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalize("My package", "My constraint", HardSoftScore.ONE_HARD, (a, b, c) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalize()
                                        .withMatchWeight((a, b, c) -> 7)
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void triPenalizeNameMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeLong("My constraint", HardSoftLongScore.ONE_HARD, (a, b, c) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalize()
                                        .withLongMatchWeight((a, b, c) -> 7L)
                                        .usingDefaultConstraintWeight(HardSoftLongScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void triPenalizeIdMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeLong("My package", "My constraint", HardSoftLongScore.ONE_HARD, (a, b, c) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalize()
                                        .withLongMatchWeight((a, b, c) -> 7L)
                                        .usingDefaultConstraintWeight(HardSoftLongScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void triPenalizeNameMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeBigDecimal("My constraint", HardSoftBigDecimalScore.ONE_HARD, (a, b, c) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalize()
                                        .withBigDecimalMatchWeight((a, b, c) -> BigDecimal.TEN)
                                        .usingDefaultConstraintWeight(HardSoftBigDecimalScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void triPenalizeIdMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeBigDecimal("My package", "My constraint", HardSoftBigDecimalScore.ONE_HARD, (a, b, c) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalize()
                                        .withBigDecimalMatchWeight((a, b, c) -> BigDecimal.TEN)
                                        .usingDefaultConstraintWeight(HardSoftBigDecimalScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void triRewardName() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .reward("My constraint", HardSoftScore.ONE_HARD);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .reward()
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void triRewardId() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .reward("My package", "My constraint", HardSoftScore.ONE_HARD);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .reward()
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void triRewardNameMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .reward("My constraint", HardSoftScore.ONE_HARD, (a, b, c) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .reward()
                                        .withMatchWeight((a, b, c) -> 7)
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void triRewardIdMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .reward("My package", "My constraint", HardSoftScore.ONE_HARD, (a, b, c) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .reward()
                                        .withMatchWeight((a, b, c) -> 7)
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void triRewardNameMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardLong("My constraint", HardSoftLongScore.ONE_HARD, (a, b, c) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .reward()
                                        .withLongMatchWeight((a, b, c) -> 7L)
                                        .usingDefaultConstraintWeight(HardSoftLongScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void triRewardIdMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardLong("My package", "My constraint", HardSoftLongScore.ONE_HARD, (a, b, c) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .reward()
                                        .withLongMatchWeight((a, b, c) -> 7L)
                                        .usingDefaultConstraintWeight(HardSoftLongScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void triRewardNameMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardBigDecimal("My constraint", HardSoftBigDecimalScore.ONE_HARD, (a, b, c) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .reward()
                                        .withBigDecimalMatchWeight((a, b, c) -> BigDecimal.TEN)
                                        .usingDefaultConstraintWeight(HardSoftBigDecimalScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void triRewardIdMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardBigDecimal("My package", "My constraint", HardSoftBigDecimalScore.ONE_HARD, (a, b, c) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .reward()
                                        .withBigDecimalMatchWeight((a, b, c) -> BigDecimal.TEN)
                                        .usingDefaultConstraintWeight(HardSoftBigDecimalScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void triImpactName() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .impact("My constraint", HardSoftScore.ONE_HARD);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .impact()
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void triImpactId() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .impact("My package", "My constraint", HardSoftScore.ONE_HARD);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .impact()
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void triImpactNameMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .impact("My constraint", HardSoftScore.ONE_HARD, (a, b, c) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .impact()
                                        .withMatchWeight((a, b, c) -> 7)
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void triImpactIdMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .impact("My package", "My constraint", HardSoftScore.ONE_HARD, (a, b, c) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .impact()
                                        .withMatchWeight((a, b, c) -> 7)
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void triImpactNameMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .impactLong("My constraint", HardSoftLongScore.ONE_HARD, (a, b, c) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .impact()
                                        .withLongMatchWeight((a, b, c) -> 7L)
                                        .usingDefaultConstraintWeight(HardSoftLongScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void triImpactIdMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .impactLong("My package", "My constraint", HardSoftLongScore.ONE_HARD, (a, b, c) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .impact()
                                        .withLongMatchWeight((a, b, c) -> 7L)
                                        .usingDefaultConstraintWeight(HardSoftLongScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void triImpactNameMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .impactBigDecimal("My constraint", HardSoftBigDecimalScore.ONE_HARD, (a, b, c) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .impact()
                                        .withBigDecimalMatchWeight((a, b, c) -> BigDecimal.TEN)
                                        .usingDefaultConstraintWeight(HardSoftBigDecimalScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void triImpactIdMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .impactBigDecimal("My package", "My constraint", HardSoftBigDecimalScore.ONE_HARD, (a, b, c) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .impact()
                                        .withBigDecimalMatchWeight((a, b, c) -> BigDecimal.TEN)
                                        .usingDefaultConstraintWeight(HardSoftBigDecimalScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    // ************************************************************************
    // Quad
    // ************************************************************************

    @Test
    void quadPenalizeName() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalize("My constraint", HardSoftScore.ONE_HARD);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalize()
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void quadPenalizeId() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalize("My package", "My constraint", HardSoftScore.ONE_HARD);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalize()
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void quadPenalizeNameMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalize("My constraint", HardSoftScore.ONE_HARD, (a, b, c, d) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalize()
                                        .withMatchWeight((a, b, c, d) -> 7)
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void quadPenalizeIdMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalize("My package", "My constraint", HardSoftScore.ONE_HARD, (a, b, c, d) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalize()
                                        .withMatchWeight((a, b, c, d) -> 7)
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void quadPenalizeNameMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeLong("My constraint", HardSoftLongScore.ONE_HARD, (a, b, c, d) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalize()
                                        .withLongMatchWeight((a, b, c, d) -> 7L)
                                        .usingDefaultConstraintWeight(HardSoftLongScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void quadPenalizeIdMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeLong("My package", "My constraint", HardSoftLongScore.ONE_HARD, (a, b, c, d) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalize()
                                        .withLongMatchWeight((a, b, c, d) -> 7L)
                                        .usingDefaultConstraintWeight(HardSoftLongScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void quadPenalizeNameMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeBigDecimal("My constraint", HardSoftBigDecimalScore.ONE_HARD, (a, b, c, d) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalize()
                                        .withBigDecimalMatchWeight((a, b, c, d) -> BigDecimal.TEN)
                                        .usingDefaultConstraintWeight(HardSoftBigDecimalScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void quadPenalizeIdMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeBigDecimal("My package", "My constraint", HardSoftBigDecimalScore.ONE_HARD, (a, b, c, d) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalize()
                                        .withBigDecimalMatchWeight((a, b, c, d) -> BigDecimal.TEN)
                                        .usingDefaultConstraintWeight(HardSoftBigDecimalScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void quadRewardName() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .reward("My constraint", HardSoftScore.ONE_HARD);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .reward()
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void quadRewardId() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .reward("My package", "My constraint", HardSoftScore.ONE_HARD);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .reward()
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void quadRewardNameMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .reward("My constraint", HardSoftScore.ONE_HARD, (a, b, c, d) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .reward()
                                        .withMatchWeight((a, b, c, d) -> 7)
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void quadRewardIdMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .reward("My package", "My constraint", HardSoftScore.ONE_HARD, (a, b, c, d) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .reward()
                                        .withMatchWeight((a, b, c, d) -> 7)
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void quadRewardNameMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardLong("My constraint", HardSoftLongScore.ONE_HARD, (a, b, c, d) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .reward()
                                        .withLongMatchWeight((a, b, c, d) -> 7L)
                                        .usingDefaultConstraintWeight(HardSoftLongScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void quadRewardIdMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardLong("My package", "My constraint", HardSoftLongScore.ONE_HARD, (a, b, c, d) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .reward()
                                        .withLongMatchWeight((a, b, c, d) -> 7L)
                                        .usingDefaultConstraintWeight(HardSoftLongScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void quadRewardNameMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardBigDecimal("My constraint", HardSoftBigDecimalScore.ONE_HARD, (a, b, c, d) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .reward()
                                        .withBigDecimalMatchWeight((a, b, c, d) -> BigDecimal.TEN)
                                        .usingDefaultConstraintWeight(HardSoftBigDecimalScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void quadRewardIdMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardBigDecimal("My package", "My constraint", HardSoftBigDecimalScore.ONE_HARD, (a, b, c, d) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .reward()
                                        .withBigDecimalMatchWeight((a, b, c, d) -> BigDecimal.TEN)
                                        .usingDefaultConstraintWeight(HardSoftBigDecimalScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void quadImpactName() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .impact("My constraint", HardSoftScore.ONE_HARD);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .impact()
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void quadImpactId() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .impact("My package", "My constraint", HardSoftScore.ONE_HARD);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .impact()
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void quadImpactNameMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .impact("My constraint", HardSoftScore.ONE_HARD, (a, b, c, d) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .impact()
                                        .withMatchWeight((a, b, c, d) -> 7)
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void quadImpactIdMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .impact("My package", "My constraint", HardSoftScore.ONE_HARD, (a, b, c, d) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .impact()
                                        .withMatchWeight((a, b, c, d) -> 7)
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void quadImpactNameMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .impactLong("My constraint", HardSoftLongScore.ONE_HARD, (a, b, c, d) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .impact()
                                        .withLongMatchWeight((a, b, c, d) -> 7L)
                                        .usingDefaultConstraintWeight(HardSoftLongScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void quadImpactIdMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .impactLong("My package", "My constraint", HardSoftLongScore.ONE_HARD, (a, b, c, d) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .impact()
                                        .withLongMatchWeight((a, b, c, d) -> 7L)
                                        .usingDefaultConstraintWeight(HardSoftLongScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void quadImpactNameMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .impactBigDecimal("My constraint", HardSoftBigDecimalScore.ONE_HARD, (a, b, c, d) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .impact()
                                        .withBigDecimalMatchWeight((a, b, c, d) -> BigDecimal.TEN)
                                        .usingDefaultConstraintWeight(HardSoftBigDecimalScore.ONE_HARD)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void quadImpactIdMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .impactBigDecimal("My package", "My constraint", HardSoftBigDecimalScore.ONE_HARD, (a, b, c, d) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .impact()
                                        .withBigDecimalMatchWeight((a, b, c, d) -> BigDecimal.TEN)
                                        .usingDefaultConstraintWeight(HardSoftBigDecimalScore.ONE_HARD)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    // ************************************************************************
    // Helper methods
    // ************************************************************************

    private static String wrap(String content) {
        return """
                import java.math.BigDecimal;
                import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
                import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
                import ai.timefold.solver.core.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScore;
                import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
                import ai.timefold.solver.core.api.score.stream.Constraint;

                class Test {
                    Constraint myConstraint(ConstraintFactory f) {
                %s
                   }
                }
                """.formatted(content);
    }

}
