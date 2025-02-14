package ai.timefold.solver.migration.v8;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

import ai.timefold.solver.migration.AbstractRecipe;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.J;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConstraintStubRecipe extends AbstractRecipe {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConstraintStubRecipe.class);
    
    private static final String UNI_STREAM = "ai.timefold.solver.core.api.score.stream.uni.UniConstraintStream";
    private static final String BI_STREAM = "ai.timefold.solver.core.api.score.stream.bi.BiConstraintStream";
    private static final String TRI_STREAM = "ai.timefold.solver.core.api.score.stream.tri.TriConstraintStream";
    private static final String QUAD_STREAM = "ai.timefold.solver.core.api.score.stream.quad.QuadConstraintStream";

    private static final MatcherMeta[] MATCHER_METAS = {
            new MatcherMeta(UNI_STREAM, "penalize(Score)"),
            new MatcherMeta(BI_STREAM, "penalize(Score)"),
            new MatcherMeta(TRI_STREAM, "penalize(Score)"),
            new MatcherMeta(QUAD_STREAM, "penalize(Score)"),
            new MatcherMeta(UNI_STREAM, "reward(Score)"),
            new MatcherMeta(BI_STREAM, "reward(Score)"),
            new MatcherMeta(TRI_STREAM, "reward(Score)"),
            new MatcherMeta(QUAD_STREAM, "reward(Score)"),
            new MatcherMeta(UNI_STREAM, "impact(Score)"),
            new MatcherMeta(BI_STREAM, "impact(Score)"),
            new MatcherMeta(TRI_STREAM, "impact(Score)"),
            new MatcherMeta(QUAD_STREAM, "impact(Score)"),

            new MatcherMeta(UNI_STREAM, "penalize(Score, ToIntFunction)"),
            new MatcherMeta(UNI_STREAM, "penalizeLong(Score, ToLongFunction)"),
            new MatcherMeta(UNI_STREAM, "penalizeBigDecimal(Score, Function)"),
            new MatcherMeta(UNI_STREAM, "reward(Score, ToIntFunction)"),
            new MatcherMeta(UNI_STREAM, "rewardLong(Score, ToLongFunction)"),
            new MatcherMeta(UNI_STREAM, "rewardBigDecimal(Score, Function)"),
            new MatcherMeta(UNI_STREAM, "impact(Score, ToIntFunction)"),
            new MatcherMeta(UNI_STREAM, "impactLong(Score, ToLongFunction)"),
            new MatcherMeta(UNI_STREAM, "impactBigDecimal(Score, Function)"),

            new MatcherMeta(BI_STREAM, "penalize(Score, ToIntBiFunction)"),
            new MatcherMeta(BI_STREAM, "penalizeLong(Score, ToLongBiFunction)"),
            new MatcherMeta(BI_STREAM, "penalizeBigDecimal(Score, BiFunction)"),
            new MatcherMeta(BI_STREAM, "reward(Score, ToIntBiFunction)"),
            new MatcherMeta(BI_STREAM, "rewardLong(Score, ToLongBiFunction)"),
            new MatcherMeta(BI_STREAM, "rewardBigDecimal(Score, BiFunction)"),
            new MatcherMeta(BI_STREAM, "impact(Score, ToIntBiFunction)"),
            new MatcherMeta(BI_STREAM, "impactLong(Score, ToLongBiFunction)"),
            new MatcherMeta(BI_STREAM, "impactBigDecimal(Score, BiFunction)"),

            new MatcherMeta(TRI_STREAM, "penalize(Score, ToIntTriFunction)"),
            new MatcherMeta(TRI_STREAM, "penalizeLong(Score, ToLongTriFunction)"),
            new MatcherMeta(TRI_STREAM, "penalizeBigDecimal(Score, TriFunction)"),
            new MatcherMeta(TRI_STREAM, "reward(Score, ToIntTriFunction)"),
            new MatcherMeta(TRI_STREAM, "rewardLong(Score, ToLongTriFunction)"),
            new MatcherMeta(TRI_STREAM, "rewardBigDecimal(Score, TriFunction)"),
            new MatcherMeta(TRI_STREAM, "impact(Score, ToIntTriFunction)"),
            new MatcherMeta(TRI_STREAM, "impactLong(Score, ToLongTriFunction)"),
            new MatcherMeta(TRI_STREAM, "impactBigDecimal(Score, TriFunction)"),

            new MatcherMeta(QUAD_STREAM, "penalize(Score, ToIntQuadFunction)"),
            new MatcherMeta(QUAD_STREAM, "penalizeLong(Score, ToLongQuadFunction)"),
            new MatcherMeta(QUAD_STREAM, "penalizeBigDecimal(Score, QuadFunction)"),
            new MatcherMeta(QUAD_STREAM, "reward(Score, ToIntQuadFunction)"),
            new MatcherMeta(QUAD_STREAM, "rewardLong(Score, ToLongQuadFunction)"),
            new MatcherMeta(QUAD_STREAM, "rewardBigDecimal(Score, QuadFunction)"),
            new MatcherMeta(QUAD_STREAM, "impact(Score, ToIntQuadFunction)"),
            new MatcherMeta(QUAD_STREAM, "impactLong(Score, ToLongQuadFunction)"),
            new MatcherMeta(QUAD_STREAM, "impactBigDecimal(Score, QuadFunction)"),
    };

    @Override
    public String getDisplayName() {
        return "ConstraintStreams: use asConstraint() methods to define constraints";
    }

    @Override
    public String getDescription() {
        return "Use `penalize().asConstraint()` and `reward().asConstraint()`" +
                " instead of the deprecated `penalize()` and `reward()` methods.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        TreeVisitor<?, ExecutionContext>[] visitors = Arrays.stream(MATCHER_METAS)
                .map(m -> new UsesMethod<>(m.methodMatcher))
                .toArray(TreeVisitor[]::new);
        return Preconditions.check(
                Preconditions.or(visitors),
                new JavaIsoVisitor<>() {

                    private final Pattern uniConstraintStreamPattern = Pattern.compile(
                            "ai.timefold.solver.core.api.score.stream.uni.UniConstraintStream");
                    private final Pattern biConstraintStreamPattern = Pattern.compile(
                            "ai.timefold.solver.core.api.score.stream.bi.BiConstraintStream");
                    private final Pattern triConstraintStreamPattern = Pattern.compile(
                            "ai.timefold.solver.core.api.score.stream.tri.TriConstraintStream");
                    private final Pattern quadConstraintStreamPattern = Pattern.compile(
                            "ai.timefold.solver.core.api.score.stream.quad.QuadConstraintStream");

                    @Override
                    public J.MethodInvocation visitMethodInvocation(J.MethodInvocation originalMethod,
                            ExecutionContext executionContext) {
                        var method = super.visitMethodInvocation(originalMethod, executionContext);

                        var matcherMeta = Arrays.stream(MATCHER_METAS)
                                .filter(m -> m.methodMatcher.matches(method))
                                .findFirst()
                                .orElse(null);
                        if (matcherMeta == null) {
                            return method;
                        }
                        var select = Objects.requireNonNull(method.getSelect());
                        var arguments = method.getArguments();

                        String templateCode;
                        var selectType = Objects.requireNonNull(select.getType());
                        if (selectType.isAssignableFrom(uniConstraintStreamPattern)) {
                            templateCode = "#{any(ai.timefold.solver.core.api.score.stream.uni.UniConstraintStream)}\n";
                        } else if (selectType.isAssignableFrom(biConstraintStreamPattern)) {
                            templateCode = "#{any(ai.timefold.solver.core.api.score.stream.bi.BiConstraintStream)}\n";
                        } else if (selectType.isAssignableFrom(triConstraintStreamPattern)) {
                            templateCode = "#{any(ai.timefold.solver.core.api.score.stream.tri.TriConstraintStream)}\n";
                        } else if (selectType.isAssignableFrom(quadConstraintStreamPattern)) {
                            templateCode = "#{any(ai.timefold.solver.core.api.score.stream.quad.QuadConstraintStream)}\n";
                        } else {
                            LOGGER.warn("Cannot refactor to ConstraintStub for deprecated method ({}).", method);
                            return method;
                        }
                        if (!matcherMeta.matchWeigherIncluded) {
                            templateCode +=
                                    "." + matcherMeta.methodName + "()\n";
                        } else {
                            templateCode +=
                                    "." + matcherMeta.methodName + "(#{any(" + matcherMeta.functionType + ")})\n";
                        }
                        templateCode += ".usingDefaultConstraintWeight(#{any(ai.timefold.solver.core.api.score.Score)})\n";
                        var template = JavaTemplate.builder(templateCode)
                                .javaParser(JAVA_PARSER)
                                .build();
                        var constraintWeightArgument = arguments.get(1);
                        if (!matcherMeta.matchWeigherIncluded) {
                            return template.apply(getCursor(),
                                    method.getCoordinates().replace(), select,
                                    constraintWeightArgument);
                        } else {
                            var matchWeigherArgument = arguments.get(2);
                            return template.apply(getCursor(),
                                    method.getCoordinates().replace(), select,
                                    matchWeigherArgument, constraintWeightArgument);
                        }
                    }
                });
    }

    private static final class MatcherMeta {

        public final MethodMatcher methodMatcher;
        public final boolean matchWeigherIncluded;
        public final String methodName; // penalize, reward or impact
        public final String functionType;

        public MatcherMeta(String select, String method) {
            String signature = select + " " +
                    method.replace("Score", "ai.timefold.solver.core.api.score.Score")
                            .replace(" ToIntFunction", " java.util.function.ToIntFunction")
                            .replace(" ToLongFunction", " java.util.function.ToLongFunction")
                            .replace(" Function", " java.util.function.Function")
                            .replace(" ToIntBiFunction", " java.util.function.ToIntBiFunction")
                            .replace(" ToLongBiFunction", " java.util.function.ToLongBiFunction")
                            .replace(" BiFunction", " java.util.function.BiFunction")
                            .replace(" ToIntTriFunction", " ai.timefold.solver.core.api.function.ToIntTriFunction")
                            .replace(" ToLongTriFunction", " ai.timefold.solver.core.api.function.ToLongTriFunction")
                            .replace(" TriFunction", " ai.timefold.solver.core.api.function.TriFunction")
                            .replace(" ToIntQuadFunction", " ai.timefold.solver.core.api.function.ToIntQuadFunction")
                            .replace(" ToLongQuadFunction", " ai.timefold.solver.core.api.function.ToLongQuadFunction")
                            .replace(" QuadFunction", " ai.timefold.solver.core.api.function.QuadFunction");
            methodMatcher = new MethodMatcher(signature);
            matchWeigherIncluded = method.contains("Function");
            if (matchWeigherIncluded) {
                this.functionType = signature.replaceFirst("^.* ([\\w\\.]+Function)\\)$", "$1");
            } else {
                this.functionType = null;
            }
            this.methodName = method.replaceFirst("\\(.*$", "");
        }
    }

}
