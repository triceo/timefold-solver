package ai.timefold.solver.core.impl.score.stream.common.tri;

import java.math.BigDecimal;
import java.util.Objects;

import ai.timefold.solver.core.api.function.ToIntTriFunction;
import ai.timefold.solver.core.api.function.ToLongTriFunction;
import ai.timefold.solver.core.api.function.TriFunction;
import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.tri.TriConstraintBuilder;
import ai.timefold.solver.core.api.score.stream.tri.TriConstraintStub;
import ai.timefold.solver.core.impl.score.stream.common.ScoreImpactType;
import ai.timefold.solver.core.impl.util.ConstantLambdaUtils;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class TriConstraintStubImpl<A, B, C> implements TriConstraintStub<A, B, C> {

    private final TriConstraintConstructor<A, B, C, ?> constraintConstructor;
    private final ScoreImpactType impactType;
    private @Nullable AbstractTriMatchWeight<A, B, C> matchWeight;

    public TriConstraintStubImpl(TriConstraintConstructor<A, B, C, ?> constraintConstructor, ScoreImpactType impactType) {
        this.constraintConstructor = Objects.requireNonNull(constraintConstructor);
        this.impactType = Objects.requireNonNull(impactType);
    }

    @Override
    public TriConstraintStub<A, B, C> withMatchWeight(ToIntTriFunction<A, B, C> matchWeigher) {
        failIfAlreadySet();
        matchWeight = new IntTriMatchWeight<>(matchWeigher);
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
    public TriConstraintStub<A, B, C> withLongMatchWeight(ToLongTriFunction<A, B, C> matchWeigher) {
        failIfAlreadySet();
        matchWeight = new LongTriMatchWeight<>(matchWeigher);
        return this;
    }

    @Override
    public TriConstraintStub<A, B, C> withBigDecimalMatchWeight(TriFunction<A, B, C, BigDecimal> matchWeigher) {
        failIfAlreadySet();
        matchWeight = new BigDecimalTriMatchWeight<>(matchWeigher);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <Score_ extends Score<Score_>> TriConstraintBuilder<A, B, C, Score_>
            usingDefaultConstraintWeight(Score_ constraintWeight) {
        var matchWeight_ = Objects.requireNonNullElseGet(matchWeight,
                () -> new IntTriMatchWeight<A, B, C>(ConstantLambdaUtils.triConstantOne()));
        return new TriConstraintBuilderImpl<>((TriConstraintConstructor<A, B, C, Score_>) constraintConstructor, impactType,
                matchWeight_, constraintWeight);
    }

}
