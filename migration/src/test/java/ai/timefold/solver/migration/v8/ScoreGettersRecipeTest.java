package ai.timefold.solver.migration.v8;

import static org.openrewrite.java.Assertions.java;

import ai.timefold.solver.migration.AbstractRecipe;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

@Execution(ExecutionMode.CONCURRENT)
class ScoreGettersRecipeTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ScoreGettersRecipe())
                .parser(AbstractRecipe.JAVA_PARSER);
    }

    @Test
    void bendableScore() {
        runTest("ai.timefold.solver.core.api.score.buildin.bendable.BendableScore",
                "BendableScore score = BendableScore.of(new int[] {1, 2}, new int[] {3, 4});",
                "int scoreLevelsSize = score.getLevelsSize();\n" +
                        "int hardScoreLevelsSize = score.getHardLevelsSize();\n" +
                        "int[] hardScores = score.getHardScores();\n" +
                        "int hardScore0 = score.getHardScore(0);\n" +
                        "int softScoreLevelsSize = score.getSoftLevelsSize();\n" +
                        "int[] softScores = score.getSoftScores();\n" +
                        "int softScore1 = score.getSoftScore(1);\n" +
                        "int initScore = score.getInitScore();\n",
                "int scoreLevelsSize = score.levelsSize();\n" +
                        "int hardScoreLevelsSize = score.hardLevelsSize();\n" +
                        "int[] hardScores = score.hardScores();\n" +
                        "int hardScore0 = score.hardScore(0);\n" +
                        "int softScoreLevelsSize = score.softLevelsSize();\n" +
                        "int[] softScores = score.softScores();\n" +
                        "int softScore1 = score.softScore(1);\n" +
                        "int initScore = score.initScore();\n");
    }

    @Test
    void bendableBigDecimalScore() {
        runTest("ai.timefold.solver.core.api.score.buildin.bendablebigdecimal.BendableBigDecimalScore",
                "BendableBigDecimalScore score = BendableBigDecimalScore.of(\n" +
                        "   new BigDecimal[] {BigDecimal.ONE},\n" +
                        "   new BigDecimal[] {BigDecimal.ONE, BigDecimal.TEN});",
                "int scoreLevelsSize = score.getLevelsSize();\n" +
                        "int hardScoreLevelsSize = score.getHardLevelsSize();\n" +
                        "BigDecimal[] hardScores = score.getHardScores();\n" +
                        "BigDecimal hardScore0 = score.getHardScore(0);\n" +
                        "int softScoreLevelsSize = score.getSoftLevelsSize();\n" +
                        "BigDecimal[] softScores = score.getSoftScores();\n" +
                        "BigDecimal softScore1 = score.getSoftScore(1);\n" +
                        "int initScore = score.getInitScore();\n",
                "int scoreLevelsSize = score.levelsSize();\n" +
                        "int hardScoreLevelsSize = score.hardLevelsSize();\n" +
                        "BigDecimal[] hardScores = score.hardScores();\n" +
                        "BigDecimal hardScore0 = score.hardScore(0);\n" +
                        "int softScoreLevelsSize = score.softLevelsSize();\n" +
                        "BigDecimal[] softScores = score.softScores();\n" +
                        "BigDecimal softScore1 = score.softScore(1);\n" +
                        "int initScore = score.initScore();\n");
    }

    @Test
    void bendableLongScore() {
        runTest("ai.timefold.solver.core.api.score.buildin.bendablelong.BendableLongScore",
                "BendableLongScore score = BendableLongScore.of(" +
                        "   new long[] {1L}, " +
                        "   new long[] {1L, 10L});",
                "int scoreLevelsSize = score.getLevelsSize();\n" +
                        "int hardScoreLevelsSize = score.getHardLevelsSize();\n" +
                        "long[] hardScores = score.getHardScores();\n" +
                        "long hardScore0 = score.getHardScore(0);\n" +
                        "int softScoreLevelsSize = score.getSoftLevelsSize();\n" +
                        "long[] softScores = score.getSoftScores();\n" +
                        "long softScore1 = score.getSoftScore(1);\n" +
                        "int initScore = score.getInitScore();\n",
                "int scoreLevelsSize = score.levelsSize();\n" +
                        "int hardScoreLevelsSize = score.hardLevelsSize();\n" +
                        "long[] hardScores = score.hardScores();\n" +
                        "long hardScore0 = score.hardScore(0);\n" +
                        "int softScoreLevelsSize = score.softLevelsSize();\n" +
                        "long[] softScores = score.softScores();\n" +
                        "long softScore1 = score.softScore(1);\n" +
                        "int initScore = score.initScore();\n");
    }

    @Test
    void hardMediumSoftScore() {
        runTest("ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore",
                "HardMediumSoftScore score = HardMediumSoftScore.of(1, 2, 3);",
                "int hardScore = score.getHardScore();\n" +
                        "int mediumScore = score.getMediumScore();\n" +
                        "int softScore = score.getSoftScore();\n" +
                        "int initScore = score.getInitScore();\n",
                "int hardScore = score.hardScore();\n" +
                        "int mediumScore = score.mediumScore();\n" +
                        "int softScore = score.softScore();\n" +
                        "int initScore = score.initScore();\n");
    }

    @Test
    void hardMediumSoftBigDecimalScore() {
        runTest("ai.timefold.solver.core.api.score.buildin.hardmediumsoftbigdecimal.HardMediumSoftBigDecimalScore",
                "HardMediumSoftBigDecimalScore score = HardMediumSoftBigDecimalScore.of(BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.TEN);",
                "BigDecimal hardScore = score.getHardScore();\n" +
                        "BigDecimal mediumScore = score.getMediumScore();\n" +
                        "BigDecimal softScore = score.getSoftScore();\n" +
                        "int initScore = score.getInitScore();\n",
                "BigDecimal hardScore = score.hardScore();\n" +
                        "BigDecimal mediumScore = score.mediumScore();\n" +
                        "BigDecimal softScore = score.softScore();\n" +
                        "int initScore = score.initScore();\n");
    }

    @Test
    void hardMediumSoftLongScore() {
        runTest("ai.timefold.solver.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore",
                "HardMediumSoftLongScore score = HardMediumSoftLongScore.of(1L, 2L, 3L);",
                "long hardScore = score.getHardScore();\n" +
                        "long mediumScore = score.getMediumScore();\n" +
                        "long softScore = score.getSoftScore();\n" +
                        "int initScore = score.getInitScore();\n",
                "long hardScore = score.hardScore();\n" +
                        "long mediumScore = score.mediumScore();\n" +
                        "long softScore = score.softScore();\n" +
                        "int initScore = score.initScore();\n");
    }

    @Test
    void hardSoftScore() {
        runTest("ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore",
                "HardSoftScore score = HardSoftScore.of(1, 2);",
                "int hardScore = score.getHardScore();\n" +
                        "int softScore = score.getSoftScore();\n" +
                        "int initScore = score.getInitScore();\n",
                "int hardScore = score.hardScore();\n" +
                        "int softScore = score.softScore();\n" +
                        "int initScore = score.initScore();\n");
    }

    @Test
    void hardSoftBigDecimalScore() {
        runTest("ai.timefold.solver.core.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScore",
                "HardSoftBigDecimalScore score = HardSoftBigDecimalScore.of(BigDecimal.ZERO, BigDecimal.ONE);",
                "BigDecimal hardScore = score.getHardScore();\n" +
                        "BigDecimal softScore = score.getSoftScore();\n" +
                        "int initScore = score.getInitScore();\n",
                "BigDecimal hardScore = score.hardScore();\n" +
                        "BigDecimal softScore = score.softScore();\n" +
                        "int initScore = score.initScore();\n");
    }

    @Test
    void hardSoftLongScore() {
        runTest("ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore",
                "HardSoftLongScore score = HardSoftLongScore.of(1L, 2L);",
                "long hardScore = score.getHardScore();\n" +
                        "long softScore = score.getSoftScore();\n" +
                        "int initScore = score.getInitScore();\n",
                "long hardScore = score.hardScore();\n" +
                        "long softScore = score.softScore();\n" +
                        "int initScore = score.initScore();\n");
    }

    @Test
    void simpleScore() {
        runTest("ai.timefold.solver.core.api.score.buildin.simple.SimpleScore",
                "SimpleScore score = SimpleScore.of(1);",
                "int value = score.getScore();\n" +
                        "int initScore = score.getInitScore();\n",
                "int value = score.score();\n" +
                        "int initScore = score.initScore();\n");
    }

    @Test
    void simpleBigDecimalScore() {
        runTest("ai.timefold.solver.core.api.score.buildin.simplebigdecimal.SimpleBigDecimalScore",
                "SimpleBigDecimalScore score = SimpleBigDecimalScore.of(BigDecimal.ONE);",
                "BigDecimal value = score.getScore();\n" +
                        "int initScore = score.getInitScore();\n",
                "BigDecimal value = score.score();\n" +
                        "int initScore = score.initScore();\n");
    }

    @Test
    void simpleLongScore() {
        runTest("ai.timefold.solver.core.api.score.buildin.simplelong.SimpleLongScore",
                "SimpleLongScore score = SimpleLongScore.of(1L);",
                "long value = score.getScore();\n" +
                        "int initScore = score.getInitScore();\n",
                "long value = score.score();\n" +
                        "int initScore = score.initScore();\n");
    }

    private void runTest(String scoreImplClassFqn, String scoreDeclaration, String before, String after) {
        rewriteRun(java(wrap(scoreImplClassFqn, scoreDeclaration, before), wrap(scoreImplClassFqn, scoreDeclaration, after)));
    }

    private static String wrap(String scoreImplClassFqn, String scoreDeclaration, String content) {
        return "import java.math.BigDecimal;\n" +
                "import " + scoreImplClassFqn + ";\n" +
                "\n" +
                "class Test {\n" +
                "    public static void main(String[] args) {\n" +
                scoreDeclaration.trim() + "\n" +
                content.trim() + "\n" +
                "    }" +
                "}\n";
    }

}
