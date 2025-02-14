package ai.timefold.solver.migration.v8;

import static org.openrewrite.java.Assertions.java;

import ai.timefold.solver.migration.AbstractRecipe;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

@Execution(ExecutionMode.CONCURRENT)
class ConstraintStubRecipeTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ConstraintStubRecipe())
                .parser(AbstractRecipe.JAVA_PARSER);
    }

    // ************************************************************************
    // Uni
    // ************************************************************************

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
                                        .asConstraint("My constraint");\
                        """)));
    }

    @Test
    void uniPenalizeWeightAndMatcher() {
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
                                        .asConstraint("My constraint");\
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
