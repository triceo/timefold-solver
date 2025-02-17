package ai.timefold.solver.core.impl.score.stream.common.quad;

import java.math.BigDecimal;
import java.util.Objects;

import ai.timefold.solver.core.api.function.QuadFunction;
import ai.timefold.solver.core.api.function.ToIntQuadFunction;
import ai.timefold.solver.core.api.function.ToLongQuadFunction;
import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.quad.QuadConstraintBuilder;
import ai.timefold.solver.core.api.score.stream.quad.QuadConstraintStub;
import ai.timefold.solver.core.impl.score.stream.common.ScoreImpactType;
import ai.timefold.solver.core.impl.util.ConstantLambdaUtils;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class QuadConstraintStubImpl<A, B, C, D> implements QuadConstraintStub<A, B, C, D> {

    private final QuadConstraintConstructor<A, B, C, D, ?> constraintConstructor;
    private final ScoreImpactType impactType;
    private @Nullable QuadMatchWeight<A, B, C, D> matchWeight;

    public QuadConstraintStubImpl(QuadConstraintConstructor<A, B, C, D, ?> constraintConstructor,
            ScoreImpactType impactType) {
        this.constraintConstructor = Objects.requireNonNull(constraintConstructor);
        this.impactType = Objects.requireNonNull(impactType);
    }

    @Override
    public QuadConstraintStub<A, B, C, D> withMatchWeight(ToIntQuadFunction<A, B, C, D> matchWeigher) {
        failIfAlreadySet();
        matchWeight = new IntQuadMatchWeight<>(matchWeigher);
        return this;
    }

    private void failIfAlreadySet() {
        if (matchWeight != null) {
            throw new IllegalStateException("""
                    Match weigher already set (%s).
                    Maybe the code calls withMatchWeight() twice?"""
                    .formatted(matchWeight));
        }
    }

    @Override
    public QuadConstraintStub<A, B, C, D> withLongMatchWeight(ToLongQuadFunction<A, B, C, D> matchWeigher) {
        failIfAlreadySet();
        matchWeight = new LongQuadMatchWeight<>(matchWeigher);
        return this;
    }

    @Override
    public QuadConstraintStub<A, B, C, D> withBigDecimalMatchWeight(QuadFunction<A, B, C, D, BigDecimal> matchWeigher) {
        failIfAlreadySet();
        matchWeight = new BigDecimalQuadMatchWeight<>(matchWeigher);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <Score_ extends Score<Score_>> QuadConstraintBuilder<A, B, C, D, Score_>
            usingDefaultConstraintWeight(Score_ constraintWeight) {
        var matchWeight_ = Objects.requireNonNullElseGet(matchWeight,
                () -> new IntQuadMatchWeight<A, B, C, D>(ConstantLambdaUtils.quadConstantOne()));
        return new QuadConstraintBuilderImpl<>((QuadConstraintConstructor<A, B, C, D, Score_>) constraintConstructor,
                impactType, matchWeight_, constraintWeight);
    }

}
