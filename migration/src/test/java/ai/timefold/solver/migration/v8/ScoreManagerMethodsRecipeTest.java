package ai.timefold.solver.migration.v8;

import static org.openrewrite.java.Assertions.java;

import ai.timefold.solver.migration.AbstractRecipe;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

@Execution(ExecutionMode.CONCURRENT)
class ScoreManagerMethodsRecipeTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ScoreManagerMethodsRecipe())
                .parser(AbstractRecipe.JAVA_PARSER);
    }

    @Test
    void summary() {
        runTest("String summary = scoreManager.getSummary(solution);",
                "String summary = scoreManager.explain(solution, SolutionUpdatePolicy.UPDATE_SCORE_ONLY).getSummary();");
    }

    @Test
    void explain() {
        runTest("ScoreExplanation explanation = scoreManager.explainScore(solution);",
                "ScoreExplanation explanation = scoreManager.explain(solution, SolutionUpdatePolicy.UPDATE_SCORE_ONLY);");
    }

    @Test
    void update() {
        runTest("Object score = scoreManager.updateScore(solution);",
                "Object score = scoreManager.update(solution, SolutionUpdatePolicy.UPDATE_SCORE_ONLY);");
    }

    private void runTest(String before, String after) {
        rewriteRun(java(
                wrap(before, false),
                wrap(after, true)));
    }

    private static String wrap(String content, boolean addImport) {
        return "import ai.timefold.solver.core.api.score.ScoreManager;\n" +
                (addImport ? "import ai.timefold.solver.core.api.solver.SolutionUpdatePolicy;\n" : "") +
                "import ai.timefold.solver.core.api.score.ScoreExplanation;\n" +
                "import ai.timefold.solver.core.api.solver.SolverFactory;\n" +
                "\n" +
                "class Test {\n" +
                "    public static void main(String[] args) {\n" +
                "       SolverFactory solverFactory = SolverFactory.create(null);\n" +
                "       ScoreManager scoreManager = ScoreManager.create(solverFactory);\n" +
                "       Object solution = null;\n" +
                "       " + content.trim() + "\n" +
                "    }" +
                "}\n";
    }

}
