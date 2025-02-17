package ai.timefold.solver.migration.v8;

import static org.openrewrite.java.Assertions.java;

import ai.timefold.solver.migration.AbstractRecipe;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

@Execution(ExecutionMode.CONCURRENT)
class AsConstraintConfigurableRecipeTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new AsConstraintConfigurableRecipe())
                .parser(AbstractRecipe.JAVA_PARSER);
    }

    // ************************************************************************
    // Uni
    // ************************************************************************

    @Test
    void uniPenalizeConfigurableName() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .penalizeConfigurable("My constraint");\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .penalizeConfigurable()
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void uniPenalizeConfigurableId() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .penalizeConfigurable("My package", "My constraint");\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .penalizeConfigurable()
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void uniPenalizeConfigurableNameMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .penalizeConfigurable("My constraint", (a) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .penalizeConfigurable((a) -> 7)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void uniPenalizeConfigurableIdMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .penalizeConfigurable("My package", "My constraint", (a) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .penalizeConfigurable((a) -> 7)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void uniPenalizeConfigurableNameMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .penalizeConfigurableLong("My constraint", (a) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .penalizeConfigurableLong((a) -> 7L)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void uniPenalizeConfigurableIdMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .penalizeConfigurableLong("My package", "My constraint", (a) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .penalizeConfigurableLong((a) -> 7L)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void uniPenalizeConfigurableNameMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .penalizeConfigurableBigDecimal("My constraint", (a) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .penalizeConfigurableBigDecimal((a) -> BigDecimal.TEN)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void uniPenalizeConfigurableIdMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .penalizeConfigurableBigDecimal("My package", "My constraint", (a) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .penalizeConfigurableBigDecimal((a) -> BigDecimal.TEN)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void uniRewardConfigurableName() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .rewardConfigurable("My constraint");\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .rewardConfigurable()
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void uniRewardConfigurableId() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .rewardConfigurable("My package", "My constraint");\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .rewardConfigurable()
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void uniRewardConfigurableNameMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .rewardConfigurable("My constraint", (a) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .rewardConfigurable((a) -> 7)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void uniRewardConfigurableIdMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .rewardConfigurable("My package", "My constraint", (a) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .rewardConfigurable((a) -> 7)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void uniRewardConfigurableNameMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .rewardConfigurableLong("My constraint", (a) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .rewardConfigurableLong((a) -> 7L)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void uniRewardConfigurableIdMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .rewardConfigurableLong("My package", "My constraint", (a) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .rewardConfigurableLong((a) -> 7L)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void uniRewardConfigurableNameMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .rewardConfigurableBigDecimal("My constraint", (a) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .rewardConfigurableBigDecimal((a) -> BigDecimal.TEN)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void uniRewardConfigurableIdMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .rewardConfigurableBigDecimal("My package", "My constraint", (a) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .rewardConfigurableBigDecimal((a) -> BigDecimal.TEN)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    // ************************************************************************
    // Bi
    // ************************************************************************

    @Test
    void biPenalizeConfigurableName() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalizeConfigurable("My constraint");\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalizeConfigurable()
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void biPenalizeConfigurableId() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalizeConfigurable("My package", "My constraint");\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalizeConfigurable()
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void biPenalizeConfigurableNameMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalizeConfigurable("My constraint", (a, b) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalizeConfigurable((a, b) -> 7)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void biPenalizeConfigurableIdMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalizeConfigurable("My package", "My constraint", (a, b) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalizeConfigurable((a, b) -> 7)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void biPenalizeConfigurableNameMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalizeConfigurableLong("My constraint", (a, b) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalizeConfigurableLong((a, b) -> 7L)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void biPenalizeConfigurableIdMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalizeConfigurableLong("My package", "My constraint", (a, b) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalizeConfigurableLong((a, b) -> 7L)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void biPenalizeConfigurableNameMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalizeConfigurableBigDecimal("My constraint", (a, b) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalizeConfigurableBigDecimal((a, b) -> BigDecimal.TEN)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void biPenalizeConfigurableIdMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalizeConfigurableBigDecimal("My package", "My constraint", (a, b) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalizeConfigurableBigDecimal((a, b) -> BigDecimal.TEN)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void biRewardConfigurableName() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .rewardConfigurable("My constraint");\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .rewardConfigurable()
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void biRewardConfigurableId() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .rewardConfigurable("My package", "My constraint");\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .rewardConfigurable()
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void biRewardConfigurableNameMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .rewardConfigurable("My constraint", (a, b) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .rewardConfigurable((a, b) -> 7)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void biRewardConfigurableIdMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .rewardConfigurable("My package", "My constraint", (a, b) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .rewardConfigurable((a, b) -> 7)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void biRewardConfigurableNameMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .rewardConfigurableLong("My constraint", (a, b) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .rewardConfigurableLong((a, b) -> 7L)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void biRewardConfigurableIdMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .rewardConfigurableLong("My package", "My constraint", (a, b) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .rewardConfigurableLong((a, b) -> 7L)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void biRewardConfigurableNameMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .rewardConfigurableBigDecimal("My constraint", (a, b) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .rewardConfigurableBigDecimal((a, b) -> BigDecimal.TEN)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void biRewardConfigurableIdMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .rewardConfigurableBigDecimal("My package", "My constraint", (a, b) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .rewardConfigurableBigDecimal((a, b) -> BigDecimal.TEN)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    // ************************************************************************
    // Tri
    // ************************************************************************

    @Test
    void triPenalizeConfigurableName() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeConfigurable("My constraint");\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeConfigurable()
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void triPenalizeConfigurableId() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeConfigurable("My package", "My constraint");\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeConfigurable()
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void triPenalizeConfigurableNameMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeConfigurable("My constraint", (a, b, c) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeConfigurable((a, b, c) -> 7)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void triPenalizeConfigurableIdMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeConfigurable("My package", "My constraint", (a, b, c) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeConfigurable((a, b, c) -> 7)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void triPenalizeConfigurableNameMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeConfigurableLong("My constraint", (a, b, c) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeConfigurableLong((a, b, c) -> 7L)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void triPenalizeConfigurableIdMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeConfigurableLong("My package", "My constraint", (a, b, c) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeConfigurableLong((a, b, c) -> 7L)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void triPenalizeConfigurableNameMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeConfigurableBigDecimal("My constraint", (a, b, c) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeConfigurableBigDecimal((a, b, c) -> BigDecimal.TEN)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void triPenalizeConfigurableIdMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeConfigurableBigDecimal("My package", "My constraint", (a, b, c) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeConfigurableBigDecimal((a, b, c) -> BigDecimal.TEN)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void triRewardConfigurableName() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardConfigurable("My constraint");\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardConfigurable()
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void triRewardConfigurableId() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardConfigurable("My package", "My constraint");\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardConfigurable()
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void triRewardConfigurableNameMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardConfigurable("My constraint", (a, b, c) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardConfigurable((a, b, c) -> 7)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void triRewardConfigurableIdMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardConfigurable("My package", "My constraint", (a, b, c) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardConfigurable((a, b, c) -> 7)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void triRewardConfigurableNameMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardConfigurableLong("My constraint", (a, b, c) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardConfigurableLong((a, b, c) -> 7L)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void triRewardConfigurableIdMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardConfigurableLong("My package", "My constraint", (a, b, c) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardConfigurableLong((a, b, c) -> 7L)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void triRewardConfigurableNameMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardConfigurableBigDecimal("My constraint", (a, b, c) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardConfigurableBigDecimal((a, b, c) -> BigDecimal.TEN)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void triRewardConfigurableIdMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardConfigurableBigDecimal("My package", "My constraint", (a, b, c) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardConfigurableBigDecimal((a, b, c) -> BigDecimal.TEN)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    // ************************************************************************
    // Quad
    // ************************************************************************

    @Test
    void quadPenalizeConfigurableName() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeConfigurable("My constraint");\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeConfigurable()
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void quadPenalizeConfigurableId() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeConfigurable("My package", "My constraint");\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeConfigurable()
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void quadPenalizeConfigurableNameMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeConfigurable("My constraint", (a, b, c, d) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeConfigurable((a, b, c, d) -> 7)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void quadPenalizeConfigurableIdMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeConfigurable("My package", "My constraint", (a, b, c, d) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeConfigurable((a, b, c, d) -> 7)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void quadPenalizeConfigurableNameMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeConfigurableLong("My constraint", (a, b, c, d) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeConfigurableLong((a, b, c, d) -> 7L)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void quadPenalizeConfigurableIdMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeConfigurableLong("My package", "My constraint", (a, b, c, d) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeConfigurableLong((a, b, c, d) -> 7L)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void quadPenalizeConfigurableNameMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeConfigurableBigDecimal("My constraint", (a, b, c, d) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeConfigurableBigDecimal((a, b, c, d) -> BigDecimal.TEN)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void quadPenalizeConfigurableIdMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeConfigurableBigDecimal("My package", "My constraint", (a, b, c, d) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeConfigurableBigDecimal((a, b, c, d) -> BigDecimal.TEN)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void quadRewardConfigurableName() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardConfigurable("My constraint");\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardConfigurable()
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void quadRewardConfigurableId() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardConfigurable("My package", "My constraint");\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardConfigurable()
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void quadRewardConfigurableNameMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardConfigurable("My constraint", (a, b, c, d) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardConfigurable((a, b, c, d) -> 7)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void quadRewardConfigurableIdMatchWeigherInt() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardConfigurable("My package", "My constraint", (a, b, c, d) -> 7);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardConfigurable((a, b, c, d) -> 7)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void quadRewardConfigurableNameMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardConfigurableLong("My constraint", (a, b, c, d) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardConfigurableLong((a, b, c, d) -> 7L)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void quadRewardConfigurableIdMatchWeigherLong() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardConfigurableLong("My package", "My constraint", (a, b, c, d) -> 7L);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardConfigurableLong((a, b, c, d) -> 7L)
                                        .asConstraint("My package.My constraint");\
                        """)));
    }

    @Test
    void quadRewardConfigurableNameMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardConfigurableBigDecimal("My constraint", (a, b, c, d) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardConfigurableBigDecimal((a, b, c, d) -> BigDecimal.TEN)
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void quadRewardConfigurableIdMatchWeigherBigDecimal() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardConfigurableBigDecimal("My package", "My constraint", (a, b, c, d) -> BigDecimal.TEN);\
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .rewardConfigurableBigDecimal((a, b, c, d) -> BigDecimal.TEN)
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
