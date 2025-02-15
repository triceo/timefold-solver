package ai.timefold.solver.migration.v8;

import static org.openrewrite.java.Assertions.java;

import ai.timefold.solver.migration.AbstractRecipe;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

@Execution(ExecutionMode.CONCURRENT)
class DefaultConstraintWeightRecipeTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new DefaultConstraintWeightRecipe())
                .parser(AbstractRecipe.JAVA_PARSER);
    }

    @Test
    void uniPenalizeWeight() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .penalize(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .penalize()
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");
                        """)));
    }

    @Test
    void uniPenalizeWeightAndIntMatcher() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .penalize(HardSoftScore.ONE_HARD, a -> 7)
                                        .asConstraint("My constraint");
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .penalize(a -> 7)
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");
                        """)));
    }

    @Test
    void uniPenalizeWeightAndLongMatcher() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .penalizeLong(HardSoftLongScore.ONE_HARD, a -> 7L)
                                        .asConstraint("My constraint");
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .penalizeLong(a -> 7L)
                                        .usingDefaultConstraintWeight(HardSoftLongScore.ONE_HARD)
                                        .asConstraint("My constraint");
                        """)));
    }

    @Test
    void uniPenalizeWeightAndBigDecimalMatcher() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .penalizeBigDecimal(HardSoftBigDecimalScore.ONE_HARD, a -> BigDecimal.ONE)
                                        .asConstraint("My constraint");
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .penalizeBigDecimal(a -> BigDecimal.ONE)
                                        .usingDefaultConstraintWeight(HardSoftBigDecimalScore.ONE_HARD)
                                        .asConstraint("My constraint");
                        """)));
    }

    @Test
    void biPenalizeWeight() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalize(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalize()
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");
                        """)));
    }

    @Test
    void biPenalizeWeightAndIntMatcher() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalize(HardSoftScore.ONE_HARD, (a, b) -> 7)
                                        .asConstraint("My constraint");
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalize((a, b) -> 7)
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");
                        """)));
    }

    @Test
    void biPenalizeWeightAndLongMatcher() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalizeLong(HardSoftLongScore.ONE_HARD, (a, b) -> 7L)
                                        .asConstraint("My constraint");
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalizeLong((a, b) -> 7L)
                                        .usingDefaultConstraintWeight(HardSoftLongScore.ONE_HARD)
                                        .asConstraint("My constraint");
                        """)));
    }

    @Test
    void biPenalizeWeightAndBigDecimalMatcher() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalizeBigDecimal(HardSoftBigDecimalScore.ONE_HARD, (a, b) -> BigDecimal.ONE)
                                        .asConstraint("My constraint");
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .penalizeBigDecimal((a, b) -> BigDecimal.ONE)
                                        .usingDefaultConstraintWeight(HardSoftBigDecimalScore.ONE_HARD)
                                        .asConstraint("My constraint");
                        """)));
    }

    @Test
    void triPenalizeWeight() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalize(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalize()
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");
                        """)));
    }

    @Test
    void triPenalizeWeightAndIntMatcher() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalize(HardSoftScore.ONE_HARD, (a, b, c) -> 7)
                                        .asConstraint("My constraint");
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalize((a, b, c) -> 7)
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");
                        """)));
    }

    @Test
    void triPenalizeWeightAndLongMatcher() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeLong(HardSoftLongScore.ONE_HARD, (a, b, c) -> 7L)
                                        .asConstraint("My constraint");
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeLong((a, b, c) -> 7L)
                                        .usingDefaultConstraintWeight(HardSoftLongScore.ONE_HARD)
                                        .asConstraint("My constraint");
                        """)));
    }

    @Test
    void triPenalizeWeightAndBigDecimalMatcher() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeBigDecimal(HardSoftBigDecimalScore.ONE_HARD,
                                            (a, b, c) -> BigDecimal.ONE)
                                        .asConstraint("My constraint");
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeBigDecimal((a, b, c) -> BigDecimal.ONE)
                                        .usingDefaultConstraintWeight(HardSoftBigDecimalScore.ONE_HARD)
                                        .asConstraint("My constraint");
                        """)));
    }

    @Test
    void quadPenalizeWeight() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalize(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalize()
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");
                        """)));
    }

    @Test
    void quadPenalizeWeightAndIntMatcher() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalize(HardSoftScore.ONE_HARD, (a, b, c, d) -> 7)
                                        .asConstraint("My constraint");
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalize((a, b, c, d) -> 7)
                                        .usingDefaultConstraintWeight(HardSoftScore.ONE_HARD)
                                        .asConstraint("My constraint");
                        """)));
    }

    @Test
    void quadPenalizeWeightAndLongMatcher() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeLong(HardSoftLongScore.ONE_HARD, (a, b, c, d) -> 7L)
                                        .asConstraint("My constraint");
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeLong((a, b, c, d) -> 7L)
                                        .usingDefaultConstraintWeight(HardSoftLongScore.ONE_HARD)
                                        .asConstraint("My constraint");
                        """)));
    }

    @Test
    void quadPenalizeWeightAndBigDecimalMatcher() {
        rewriteRun(java(
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeBigDecimal(HardSoftBigDecimalScore.ONE_HARD,
                                            (a, b, c, d) -> BigDecimal.ONE)
                                        .asConstraint("My constraint");
                        """),
                wrap("""
                                return f.forEach(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .join(String.class)
                                        .penalizeBigDecimal((a, b, c, d) -> BigDecimal.ONE)
                                        .usingDefaultConstraintWeight(HardSoftBigDecimalScore.ONE_HARD)
                                        .asConstraint("My constraint");
                        """)));
    }

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
