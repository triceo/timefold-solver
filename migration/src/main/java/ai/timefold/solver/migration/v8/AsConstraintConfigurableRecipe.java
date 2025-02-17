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

public final class AsConstraintConfigurableRecipe extends AbstractRecipe {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsConstraintConfigurableRecipe.class);

    private static final MatcherMeta[] MATCHER_METAS = {
            new MatcherMeta("ConstraintStream", "penalizeConfigurable(String)"),
            new MatcherMeta("ConstraintStream", "penalizeConfigurable(String, String)"),
            new MatcherMeta("ConstraintStream", "rewardConfigurable(String)"),
            new MatcherMeta("ConstraintStream", "rewardConfigurable(String, String)"),
            new MatcherMeta("ConstraintStream", "impactConfigurable(String)"),
            new MatcherMeta("ConstraintStream", "impactConfigurable(String, String)"),

            new MatcherMeta("UniConstraintStream", "penalizeConfigurable(String, ToIntFunction)"),
            new MatcherMeta("UniConstraintStream", "penalizeConfigurable(String, String, ToIntFunction)"),
            new MatcherMeta("UniConstraintStream", "penalizeConfigurableLong(String, ToLongFunction)"),
            new MatcherMeta("UniConstraintStream", "penalizeConfigurableLong(String, String, ToLongFunction)"),
            new MatcherMeta("UniConstraintStream", "penalizeConfigurableBigDecimal(String, Function)"),
            new MatcherMeta("UniConstraintStream", "penalizeConfigurableBigDecimal(String, String, Function)"),
            new MatcherMeta("UniConstraintStream", "rewardConfigurable(String, ToIntFunction)"),
            new MatcherMeta("UniConstraintStream", "rewardConfigurable(String, String, ToIntFunction)"),
            new MatcherMeta("UniConstraintStream", "rewardConfigurableLong(String, ToLongFunction)"),
            new MatcherMeta("UniConstraintStream", "rewardConfigurableLong(String, String, ToLongFunction)"),
            new MatcherMeta("UniConstraintStream", "rewardConfigurableBigDecimal(String, Function)"),
            new MatcherMeta("UniConstraintStream", "rewardConfigurableBigDecimal(String, String, Function)"),
            new MatcherMeta("UniConstraintStream", "impactConfigurable(String, ToIntFunction)"),
            new MatcherMeta("UniConstraintStream", "impactConfigurable(String, String, ToIntFunction)"),
            new MatcherMeta("UniConstraintStream", "impactConfigurableLong(String, ToLongFunction)"),
            new MatcherMeta("UniConstraintStream", "impactConfigurableLong(String, String, ToLongFunction)"),
            new MatcherMeta("UniConstraintStream", "impactConfigurableBigDecimal(String, Function)"),
            new MatcherMeta("UniConstraintStream", "impactConfigurableBigDecimal(String, String, Function)"),

            new MatcherMeta("BiConstraintStream", "penalizeConfigurable(String, ToIntBiFunction)"),
            new MatcherMeta("BiConstraintStream", "penalizeConfigurable(String, String, ToIntBiFunction)"),
            new MatcherMeta("BiConstraintStream", "penalizeConfigurableLong(String, ToLongBiFunction)"),
            new MatcherMeta("BiConstraintStream", "penalizeConfigurableLong(String, String, ToLongBiFunction)"),
            new MatcherMeta("BiConstraintStream", "penalizeConfigurableBigDecimal(String, BiFunction)"),
            new MatcherMeta("BiConstraintStream", "penalizeConfigurableBigDecimal(String, String, BiFunction)"),
            new MatcherMeta("BiConstraintStream", "rewardConfigurable(String, ToIntBiFunction)"),
            new MatcherMeta("BiConstraintStream", "rewardConfigurable(String, String, ToIntBiFunction)"),
            new MatcherMeta("BiConstraintStream", "rewardConfigurableLong(String, ToLongBiFunction)"),
            new MatcherMeta("BiConstraintStream", "rewardConfigurableLong(String, String, ToLongBiFunction)"),
            new MatcherMeta("BiConstraintStream", "rewardConfigurableBigDecimal(String, BiFunction)"),
            new MatcherMeta("BiConstraintStream", "rewardConfigurableBigDecimal(String, String, BiFunction)"),
            new MatcherMeta("BiConstraintStream", "impactConfigurable(String, ToIntBiFunction)"),
            new MatcherMeta("BiConstraintStream", "impactConfigurable(String, String, ToIntBiFunction)"),
            new MatcherMeta("BiConstraintStream", "impactConfigurableLong(String, ToLongBiFunction)"),
            new MatcherMeta("BiConstraintStream", "impactConfigurableLong(String, String, ToLongBiFunction)"),
            new MatcherMeta("BiConstraintStream", "impactConfigurableBigDecimal(String, BiFunction)"),
            new MatcherMeta("BiConstraintStream", "impactConfigurableBigDecimal(String, String, BiFunction)"),

            new MatcherMeta("TriConstraintStream", "penalizeConfigurable(String, ToIntTriFunction)"),
            new MatcherMeta("TriConstraintStream", "penalizeConfigurable(String, String, ToIntTriFunction)"),
            new MatcherMeta("TriConstraintStream", "penalizeConfigurableLong(String, ToLongTriFunction)"),
            new MatcherMeta("TriConstraintStream", "penalizeConfigurableLong(String, String, ToLongTriFunction)"),
            new MatcherMeta("TriConstraintStream", "penalizeConfigurableBigDecimal(String, TriFunction)"),
            new MatcherMeta("TriConstraintStream", "penalizeConfigurableBigDecimal(String, String, TriFunction)"),
            new MatcherMeta("TriConstraintStream", "rewardConfigurable(String, ToIntTriFunction)"),
            new MatcherMeta("TriConstraintStream", "rewardConfigurable(String, String, ToIntTriFunction)"),
            new MatcherMeta("TriConstraintStream", "rewardConfigurableLong(String, ToLongTriFunction)"),
            new MatcherMeta("TriConstraintStream", "rewardConfigurableLong(String, String, ToLongTriFunction)"),
            new MatcherMeta("TriConstraintStream", "rewardConfigurableBigDecimal(String, TriFunction)"),
            new MatcherMeta("TriConstraintStream", "rewardConfigurableBigDecimal(String, String, TriFunction)"),
            new MatcherMeta("TriConstraintStream", "impactConfigurable(String, ToIntTriFunction)"),
            new MatcherMeta("TriConstraintStream", "impactConfigurable(String, String, ToIntTriFunction)"),
            new MatcherMeta("TriConstraintStream", "impactConfigurableLong(String, ToLongTriFunction)"),
            new MatcherMeta("TriConstraintStream", "impactConfigurableLong(String, String, ToLongTriFunction)"),
            new MatcherMeta("TriConstraintStream", "impactConfigurableBigDecimal(String, TriFunction)"),
            new MatcherMeta("TriConstraintStream", "impactConfigurableBigDecimal(String, String, TriFunction)"),

            new MatcherMeta("QuadConstraintStream", "penalizeConfigurable(String, ToIntQuadFunction)"),
            new MatcherMeta("QuadConstraintStream", "penalizeConfigurable(String, String, ToIntQuadFunction)"),
            new MatcherMeta("QuadConstraintStream", "penalizeConfigurableLong(String, ToLongQuadFunction)"),
            new MatcherMeta("QuadConstraintStream", "penalizeConfigurableLong(String, String, ToLongQuadFunction)"),
            new MatcherMeta("QuadConstraintStream", "penalizeConfigurableBigDecimal(String, QuadFunction)"),
            new MatcherMeta("QuadConstraintStream", "penalizeConfigurableBigDecimal(String, String, QuadFunction)"),
            new MatcherMeta("QuadConstraintStream", "rewardConfigurable(String, ToIntQuadFunction)"),
            new MatcherMeta("QuadConstraintStream", "rewardConfigurable(String, String, ToIntQuadFunction)"),
            new MatcherMeta("QuadConstraintStream", "rewardConfigurableLong(String, ToLongQuadFunction)"),
            new MatcherMeta("QuadConstraintStream", "rewardConfigurableLong(String, String, ToLongQuadFunction)"),
            new MatcherMeta("QuadConstraintStream", "rewardConfigurableBigDecimal(String, QuadFunction)"),
            new MatcherMeta("QuadConstraintStream", "rewardConfigurableBigDecimal(String, String, QuadFunction)"),
            new MatcherMeta("QuadConstraintStream", "impactConfigurable(String, ToIntQuadFunction)"),
            new MatcherMeta("QuadConstraintStream", "impactConfigurable(String, String, ToIntQuadFunction)"),
            new MatcherMeta("QuadConstraintStream", "impactConfigurableLong(String, ToLongQuadFunction)"),
            new MatcherMeta("QuadConstraintStream", "impactConfigurableLong(String, String, ToLongQuadFunction)"),
            new MatcherMeta("QuadConstraintStream", "impactConfigurableBigDecimal(String, QuadFunction)"),
            new MatcherMeta("QuadConstraintStream", "impactConfigurableBigDecimal(String, String, QuadFunction)"),
    };

    @Override
    public String getDisplayName() {
        return "ConstraintStreams: use asConstraint() methods to define configurable constraints";
    }

    @Override
    public String getDescription() {
        // These methods cannot be fully migrated,
        // because the new methods expect a fixed value of constraint weight,
        // which we have no way of determining from the deprecated methods.
        return """
                Use `penalizeConfigurable().asConstraint()` and `rewardConfigurable().asConstraint()`
                instead of the deprecated `penalizeConfigurable(String)` and `rewardConfigurable(String)` methods.
                They are also deprecated, but less so.""";
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
                            templateCode += "." + matcherMeta.methodName + "()\n";
                        } else {
                            templateCode += "." + matcherMeta.methodName + "(#{any(" + matcherMeta.functionType + ")})\n";
                        }
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
                            if (!matcherMeta.matchWeigherIncluded) {
                                return template.apply(getCursor(),
                                        method.getCoordinates().replace(), select,
                                        constraintNameArgument);
                            } else {
                                var matcherWeightArgument = arguments.get(1);
                                return template.apply(getCursor(),
                                        method.getCoordinates().replace(), select,
                                        matcherWeightArgument, constraintNameArgument);
                            }
                        } else {
                            var constraintPackageArgument = arguments.get(0);
                            var constraintNameArgument = arguments.get(1);
                            var constraintName = mergeExpressions(constraintPackageArgument, constraintNameArgument);
                            if (!matcherMeta.matchWeigherIncluded) {
                                return template.apply(getCursor(),
                                        method.getCoordinates().replace(), select,
                                        constraintName);
                            } else {
                                var matchWeigherArgument = arguments.get(2);
                                return template.apply(getCursor(),
                                        method.getCoordinates().replace(), select,
                                        matchWeigherArgument, constraintName);
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
            signature += " " + method.replace("ToIntFunction", " java.util.function.ToIntFunction")
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
