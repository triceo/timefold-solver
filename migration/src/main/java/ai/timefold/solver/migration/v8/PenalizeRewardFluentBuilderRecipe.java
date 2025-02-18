package ai.timefold.solver.migration.v8;

import java.util.Objects;
import java.util.regex.Pattern;

import ai.timefold.solver.migration.AbstractRecipe;

import org.openrewrite.ExecutionContext;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

public final class PenalizeRewardFluentBuilderRecipe extends AbstractRecipe {

    @Override
    public String getDisplayName() {
        return "ConstraintStreams: penalize/reward no-arg overloads";
    }

    @Override
    public String getDescription() {
        return """
                Use `penalize()` and `reward()` fluent builders \
                instead of the deprecated `penalize(Score)` and `reward(Score)` methods.""";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<>() {

            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation originalMethod,
                    ExecutionContext executionContext) {
                var method = super.visitMethodInvocation(originalMethod, executionContext);

                // Eliminate methods not on *ConstraintStream.
                var select = Objects.requireNonNull(method.getSelect());
                var selectType = extractStreamFqn(select.getType());
                if (selectType == null) {
                    return method;
                }

                // Eliminate methods that are not penalize, reward or impact.
                var methodName = method.getSimpleName();
                if (!(methodName.startsWith("penalize") || methodName.startsWith("reward")
                        || methodName.startsWith("impact"))) {
                    return method;
                }

                // Only include methods that have a single Score argument,
                // or a Score and a Function argument.
                var methodArguments = method.getArguments();
                var methodArgumentCount = methodArguments.size();
                if (methodArgumentCount == 0 || methodArgumentCount > 2) {
                    return method;
                }

                var firstArgument = methodArguments.get(0);
                if (firstArgument.getType() == null) {
                    // For some reason, this is happening on methods with no arguments
                    // even though they should have been already excluded above.
                    return method;
                }
                var isFirstArgumentScore = firstArgument.getType()
                        .isAssignableFrom(Pattern.compile("ai.timefold.solver.core.api.score.Score"));
                if (!isFirstArgumentScore) {
                    return method;
                }

                String functionType = null;
                if (methodArgumentCount == 2) {
                    var secondArgument = methodArguments.get(1);
                    functionType = extractFunctionFqn(secondArgument.getType());
                    if (functionType == null) {
                        return method;
                    }
                }

                var templateCode = "#{any(" + selectType + ")}\n";
                if (functionType != null) {
                    var sanitizedImpactType = switch (methodName) {
                        case "penalize", "reward", "impact" -> methodName + "Weighted";
                        case "penalizeLong", "rewardLong", "impactLong" -> methodName.replace("Long", "") + "WeightedLong";
                        case "penalizeBigDecimal", "rewardBigDecimal", "impactBigDecimal" ->
                            methodName.replace("BigDecimal", "") + "WeightedBigDecimal";
                        default -> methodName;
                    };
                    var secondArgument = methodArguments.get(1);
                    var secondArgumentType = extractFunctionFqn(secondArgument.getType());
                    templateCode += "." + sanitizedImpactType + "(#{any(" + secondArgumentType + ")})\n";
                } else {
                    var sanitizedImpactType = switch (methodName) {
                        case "penalize", "penalizeLong", "penalizeBigDecimal" -> "penalize";
                        case "reward", "rewardLong", "rewardBigDecimal" -> "reward";
                        case "impact", "impactLong", "impactBigDecimal" -> "impact";
                        default -> methodName;
                    };
                    templateCode += "." + sanitizedImpactType + "()\n";

                }
                templateCode += ".usingDefaultConstraintWeight(#{any(ai.timefold.solver.core.api.score.Score)})\n";
                var template = JavaTemplate.builder(templateCode)
                        .javaParser(JAVA_PARSER)
                        .build();
                var constraintWeightArgument = methodArguments.get(0);
                if (functionType == null) {
                    return template.apply(getCursor(),
                            method.getCoordinates().replace(), select,
                            constraintWeightArgument);
                } else {
                    var matchWeigherArgument = methodArguments.get(1);
                    return template.apply(getCursor(),
                            method.getCoordinates().replace(), select,
                            matchWeigherArgument, constraintWeightArgument);
                }
            }
        };
    }

    private static String extractStreamFqn(JavaType type) {
        if (type.isAssignableFrom(Pattern.compile("ai.timefold.solver.core.api.score.stream.uni.UniConstraintStream"))) {
            return "ai.timefold.solver.core.api.score.stream.uni.UniConstraintStream";
        } else if (type.isAssignableFrom(Pattern.compile("ai.timefold.solver.core.api.score.stream.bi.BiConstraintStream"))) {
            return "ai.timefold.solver.core.api.score.stream.bi.BiConstraintStream";
        } else if (type.isAssignableFrom(Pattern.compile("ai.timefold.solver.core.api.score.stream.tri.TriConstraintStream"))) {
            return "ai.timefold.solver.core.api.score.stream.tri.TriConstraintStream";
        } else if (type
                .isAssignableFrom(Pattern.compile("ai.timefold.solver.core.api.score.stream.quad.QuadConstraintStream"))) {
            return "ai.timefold.solver.core.api.score.stream.quad.QuadConstraintStream";
        } else {
            return null;
        }
    }

    private static String extractFunctionFqn(JavaType type) {
        if (type.isAssignableFrom(Pattern.compile("java.util.function.Function"))) {
            return "java.util.function.Function";
        } else if (type.isAssignableFrom(Pattern.compile("java.util.function.ToIntFunction"))) {
            return "java.util.function.ToIntFunction";
        } else if (type.isAssignableFrom(Pattern.compile("java.util.function.ToLongFunction"))) {
            return "java.util.function.ToLongFunction";
        } else if (type.isAssignableFrom(Pattern.compile("java.util.function.BiFunction"))) {
            return "java.util.function.BiFunction";
        } else if (type.isAssignableFrom(Pattern.compile("java.util.function.ToIntBiFunction"))) {
            return "java.util.function.ToIntBiFunction";
        } else if (type.isAssignableFrom(Pattern.compile("java.util.function.ToLongBiFunction"))) {
            return "java.util.function.ToLongBiFunction";
        } else if (type.isAssignableFrom(Pattern.compile("ai.timefold.solver.core.api.function.TriFunction"))) {
            return "ai.timefold.solver.core.api.function.TriFunction";
        } else if (type.isAssignableFrom(Pattern.compile("ai.timefold.solver.core.api.function.ToIntTriFunction"))) {
            return "ai.timefold.solver.core.api.function.ToIntTriFunction";
        } else if (type.isAssignableFrom(Pattern.compile("ai.timefold.solver.core.api.function.ToLongTriFunction"))) {
            return "ai.timefold.solver.core.api.function.ToLongTriFunction";
        } else if (type.isAssignableFrom(Pattern.compile("ai.timefold.solver.core.api.function.QuadFunction"))) {
            return "ai.timefold.solver.core.api.function.QuadFunction";
        } else if (type.isAssignableFrom(Pattern.compile("ai.timefold.solver.core.api.function.ToIntQuadFunction"))) {
            return "ai.timefold.solver.core.api.function.ToIntQuadFunction";
        } else if (type.isAssignableFrom(Pattern.compile("ai.timefold.solver.core.api.function.ToLongQuadFunction"))) {
            return "ai.timefold.solver.core.api.function.ToLongQuadFunction";
        } else {
            return null;
        }
    }

}
