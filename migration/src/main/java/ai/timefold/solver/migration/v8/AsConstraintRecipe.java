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
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AsConstraintRecipe extends AbstractRecipe {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsConstraintRecipe.class);

    private static final MatcherMeta[] MATCHER_METAS = {
            new MatcherMeta("ConstraintStream", "penalize(String, Score)"),
            new MatcherMeta("ConstraintStream", "penalize(String, String, Score)"),
            new MatcherMeta("ConstraintStream", "reward(String, Score)"),
            new MatcherMeta("ConstraintStream", "reward(String, String, Score)"),
            new MatcherMeta("ConstraintStream", "impact(String, Score)"),
            new MatcherMeta("ConstraintStream", "impact(String, String, Score)"),

            new MatcherMeta("UniConstraintStream", "penalize(String, Score, ToIntFunction)"),
            new MatcherMeta("UniConstraintStream", "penalize(String, String, Score, ToIntFunction)"),
            new MatcherMeta("UniConstraintStream", "penalizeLong(String, Score, ToLongFunction)"),
            new MatcherMeta("UniConstraintStream", "penalizeLong(String, String, Score, ToLongFunction)"),
            new MatcherMeta("UniConstraintStream", "penalizeBigDecimal(String, Score, Function)"),
            new MatcherMeta("UniConstraintStream", "penalizeBigDecimal(String, String, Score, Function)"),
            new MatcherMeta("UniConstraintStream", "reward(String, Score, ToIntFunction)"),
            new MatcherMeta("UniConstraintStream", "reward(String, String, Score, ToIntFunction)"),
            new MatcherMeta("UniConstraintStream", "rewardLong(String, Score, ToLongFunction)"),
            new MatcherMeta("UniConstraintStream", "rewardLong(String, String, Score, ToLongFunction)"),
            new MatcherMeta("UniConstraintStream", "rewardBigDecimal(String, Score, Function)"),
            new MatcherMeta("UniConstraintStream", "rewardBigDecimal(String, String, Score, Function)"),
            new MatcherMeta("UniConstraintStream", "impact(String, Score, ToIntFunction)"),
            new MatcherMeta("UniConstraintStream", "impact(String, String, Score, ToIntFunction)"),
            new MatcherMeta("UniConstraintStream", "impactLong(String, Score, ToLongFunction)"),
            new MatcherMeta("UniConstraintStream", "impactLong(String, String, Score, ToLongFunction)"),
            new MatcherMeta("UniConstraintStream", "impactBigDecimal(String, Score, Function)"),
            new MatcherMeta("UniConstraintStream", "impactBigDecimal(String, String, Score, Function)"),

            new MatcherMeta("BiConstraintStream", "penalize(String, Score, ToIntBiFunction)"),
            new MatcherMeta("BiConstraintStream", "penalize(String, String, Score, ToIntBiFunction)"),
            new MatcherMeta("BiConstraintStream", "penalizeLong(String, Score, ToLongBiFunction)"),
            new MatcherMeta("BiConstraintStream", "penalizeLong(String, String, Score, ToLongBiFunction)"),
            new MatcherMeta("BiConstraintStream", "penalizeBigDecimal(String, Score, BiFunction)"),
            new MatcherMeta("BiConstraintStream", "penalizeBigDecimal(String, String, Score, BiFunction)"),
            new MatcherMeta("BiConstraintStream", "reward(String, Score, ToIntBiFunction)"),
            new MatcherMeta("BiConstraintStream", "reward(String, String, Score, ToIntBiFunction)"),
            new MatcherMeta("BiConstraintStream", "rewardLong(String, Score, ToLongBiFunction)"),
            new MatcherMeta("BiConstraintStream", "rewardLong(String, String, Score, ToLongBiFunction)"),
            new MatcherMeta("BiConstraintStream", "rewardBigDecimal(String, Score, BiFunction)"),
            new MatcherMeta("BiConstraintStream", "rewardBigDecimal(String, String, Score, BiFunction)"),
            new MatcherMeta("BiConstraintStream", "impact(String, Score, ToIntBiFunction)"),
            new MatcherMeta("BiConstraintStream", "impact(String, String, Score, ToIntBiFunction)"),
            new MatcherMeta("BiConstraintStream", "impactLong(String, Score, ToLongBiFunction)"),
            new MatcherMeta("BiConstraintStream", "impactLong(String, String, Score, ToLongBiFunction)"),
            new MatcherMeta("BiConstraintStream", "impactBigDecimal(String, Score, BiFunction)"),
            new MatcherMeta("BiConstraintStream", "impactBigDecimal(String, String, Score, BiFunction)"),

            new MatcherMeta("TriConstraintStream", "penalize(String, Score, ToIntTriFunction)"),
            new MatcherMeta("TriConstraintStream", "penalize(String, String, Score, ToIntTriFunction)"),
            new MatcherMeta("TriConstraintStream", "penalizeLong(String, Score, ToLongTriFunction)"),
            new MatcherMeta("TriConstraintStream", "penalizeLong(String, String, Score, ToLongTriFunction)"),
            new MatcherMeta("TriConstraintStream", "penalizeBigDecimal(String, Score, TriFunction)"),
            new MatcherMeta("TriConstraintStream", "penalizeBigDecimal(String, String, Score, TriFunction)"),
            new MatcherMeta("TriConstraintStream", "reward(String, Score, ToIntTriFunction)"),
            new MatcherMeta("TriConstraintStream", "reward(String, String, Score, ToIntTriFunction)"),
            new MatcherMeta("TriConstraintStream", "rewardLong(String, Score, ToLongTriFunction)"),
            new MatcherMeta("TriConstraintStream", "rewardLong(String, String, Score, ToLongTriFunction)"),
            new MatcherMeta("TriConstraintStream", "rewardBigDecimal(String, Score, TriFunction)"),
            new MatcherMeta("TriConstraintStream", "rewardBigDecimal(String, String, Score, TriFunction)"),
            new MatcherMeta("TriConstraintStream", "impact(String, Score, ToIntTriFunction)"),
            new MatcherMeta("TriConstraintStream", "impact(String, String, Score, ToIntTriFunction)"),
            new MatcherMeta("TriConstraintStream", "impactLong(String, Score, ToLongTriFunction)"),
            new MatcherMeta("TriConstraintStream", "impactLong(String, String, Score, ToLongTriFunction)"),
            new MatcherMeta("TriConstraintStream", "impactBigDecimal(String, Score, TriFunction)"),
            new MatcherMeta("TriConstraintStream", "impactBigDecimal(String, String, Score, TriFunction)"),

            new MatcherMeta("QuadConstraintStream", "penalize(String, Score, ToIntQuadFunction)"),
            new MatcherMeta("QuadConstraintStream", "penalize(String, String, Score, ToIntQuadFunction)"),
            new MatcherMeta("QuadConstraintStream", "penalizeLong(String, Score, ToLongQuadFunction)"),
            new MatcherMeta("QuadConstraintStream", "penalizeLong(String, String, Score, ToLongQuadFunction)"),
            new MatcherMeta("QuadConstraintStream", "penalizeBigDecimal(String, Score, QuadFunction)"),
            new MatcherMeta("QuadConstraintStream", "penalizeBigDecimal(String, String, Score, QuadFunction)"),
            new MatcherMeta("QuadConstraintStream", "reward(String, Score, ToIntQuadFunction)"),
            new MatcherMeta("QuadConstraintStream", "reward(String, String, Score, ToIntQuadFunction)"),
            new MatcherMeta("QuadConstraintStream", "rewardLong(String, Score, ToLongQuadFunction)"),
            new MatcherMeta("QuadConstraintStream", "rewardLong(String, String, Score, ToLongQuadFunction)"),
            new MatcherMeta("QuadConstraintStream", "rewardBigDecimal(String, Score, QuadFunction)"),
            new MatcherMeta("QuadConstraintStream", "rewardBigDecimal(String, String, Score, QuadFunction)"),
            new MatcherMeta("QuadConstraintStream", "impact(String, Score, ToIntQuadFunction)"),
            new MatcherMeta("QuadConstraintStream", "impact(String, String, Score, ToIntQuadFunction)"),
            new MatcherMeta("QuadConstraintStream", "impactLong(String, Score, ToLongQuadFunction)"),
            new MatcherMeta("QuadConstraintStream", "impactLong(String, String, Score, ToLongQuadFunction)"),
            new MatcherMeta("QuadConstraintStream", "impactBigDecimal(String, Score, QuadFunction)"),
            new MatcherMeta("QuadConstraintStream", "impactBigDecimal(String, String, Score, QuadFunction)"),
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
                            LOGGER.warn("Cannot refactor to asConstraint() method for deprecated method ({}).", method);
                            return method;
                        }

                        if (!matcherMeta.matchWeigherIncluded) {
                            var sanitizedImpactType = switch (matcherMeta.methodName) {
                                case "penalize", "penalizeLong", "penalizeBigDecimal" -> "penalize";
                                case "reward", "rewardLong", "rewardBigDecimal" -> "reward";
                                case "impact", "impactLong", "impactBigDecimal" -> "impact";
                                default -> matcherMeta.methodName;
                            };
                            templateCode += "." + sanitizedImpactType + "()\n";
                        } else {
                            var sanitizedImpactType = switch (matcherMeta.methodName) {
                                case "penalize", "reward", "impact" -> matcherMeta.methodName + "Weighted";
                                case "penalizeLong", "rewardLong", "impactLong" ->
                                    matcherMeta.methodName.replace("Long", "") + "WeightedLong";
                                case "penalizeBigDecimal", "rewardBigDecimal", "impactBigDecimal" ->
                                    matcherMeta.methodName.replace("BigDecimal", "") + "WeightedBigDecimal";
                                default -> matcherMeta.methodName;
                            };
                            templateCode += "." + sanitizedImpactType + "(#{any(" + matcherMeta.functionType + ")})\n";
                        }
                        templateCode += ".usingDefaultConstraintWeight(#{any(ai.timefold.solver.core.api.score.Score)})\n";
                        if (!matcherMeta.constraintPackageIncluded) {
                            templateCode += ".asConstraint(#{any(String)})";
                        } else {
                            templateCode += ".asConstraint(\"#{}\")";
                        }
                        var template = JavaTemplate.builder(templateCode)
                                .javaParser(JAVA_PARSER)
                                .build();
                        if (!matcherMeta.constraintPackageIncluded) {
                            var constraintNameArgument = arguments.get(0);
                            var constraintWeightArgument = arguments.get(1);
                            if (!matcherMeta.matchWeigherIncluded) {
                                return template.apply(getCursor(),
                                        method.getCoordinates().replace(), select,
                                        constraintWeightArgument, constraintNameArgument);
                            } else {
                                var matchWeigherArgument = arguments.get(2);
                                return template.apply(getCursor(),
                                        method.getCoordinates().replace(), select,
                                        matchWeigherArgument, constraintWeightArgument, constraintNameArgument);
                            }
                        } else {
                            var constraintPackageArgument = arguments.get(0);
                            var constraintNameArgument = arguments.get(1);
                            var constraintName = mergeExpressions(constraintPackageArgument, constraintNameArgument);
                            var constraintWeightArgument = arguments.get(2);
                            if (!matcherMeta.matchWeigherIncluded) {
                                return template.apply(getCursor(),
                                        method.getCoordinates().replace(), select,
                                        constraintWeightArgument, constraintName);
                            } else {
                                var matchWeigherArgument = arguments.get(3);
                                return template.apply(getCursor(),
                                        method.getCoordinates().replace(), select,
                                        matchWeigherArgument, constraintWeightArgument, constraintName);
                            }
                        }
                    }
                });
    }

    private String mergeExpressions(Expression constraintPackage, Expression constraintName) {
        return constraintPackage.toString() + "." + constraintName.toString();
    }

    private static class MatcherMeta {

        public final MethodMatcher methodMatcher;
        public final boolean constraintPackageIncluded;
        public final boolean matchWeigherIncluded;
        public final String methodName; // penalize, reward or impact
        public final String functionType;

        public MatcherMeta(String select, String method) {
            String signature;
            if (select.equals("ConstraintStream")) {
                signature = "ai.timefold.solver.core.api.score.stream.ConstraintStream";
            } else if (select.equals("UniConstraintStream")) {
                signature = "ai.timefold.solver.core.api.score.stream.uni.UniConstraintStream";
            } else if (select.equals("BiConstraintStream")) {
                signature = "ai.timefold.solver.core.api.score.stream.bi.BiConstraintStream";
            } else if (select.equals("TriConstraintStream")) {
                signature = "ai.timefold.solver.core.api.score.stream.tri.TriConstraintStream";
            } else if (select.equals("QuadConstraintStream")) {
                signature = "ai.timefold.solver.core.api.score.stream.quad.QuadConstraintStream";
            } else {
                throw new IllegalArgumentException("Invalid select (" + select + ").");
            }
            signature += " " + method.replace(" Score", " ai.timefold.solver.core.api.score.Score")
                    .replace("ToIntFunction", " java.util.function.ToIntFunction")
                    .replace("ToLongFunction", " java.util.function.ToLongFunction")
                    .replace("ToIntBiFunction", " java.util.function.ToIntBiFunction")
                    .replace("ToLongBiFunction", " java.util.function.ToLongBiFunction")
                    .replace("ToIntTriFunction", " ai.timefold.solver.core.api.function.ToIntTriFunction")
                    .replace("ToLongTriFunction", " ai.timefold.solver.core.api.function.ToLongTriFunction")
                    .replace("ToIntQuadFunction", " ai.timefold.solver.core.api.function.ToIntQuadFunction")
                    .replace(" ToLongQuadFunction", " ai.timefold.solver.core.api.function.ToLongQuadFunction")
                    .replace(" QuadFunction", " ai.timefold.solver.core.api.function.QuadFunction")
                    .replace(" TriFunction", " ai.timefold.solver.core.api.function.TriFunction")
                    .replace(" BiFunction", " java.util.function.BiFunction")
                    .replace(" Function", " java.util.function.Function");
            methodMatcher = new MethodMatcher(signature);
            constraintPackageIncluded = method.contains("String, String");
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
