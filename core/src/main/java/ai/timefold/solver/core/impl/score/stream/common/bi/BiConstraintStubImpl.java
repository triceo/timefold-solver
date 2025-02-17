package ai.timefold.solver.core.impl.score.stream.common.bi;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.ToIntBiFunction;
import java.util.function.ToLongBiFunction;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.bi.BiConstraintBuilder;
import ai.timefold.solver.core.api.score.stream.bi.BiConstraintStub;
import ai.timefold.solver.core.impl.score.stream.common.ScoreImpactType;
import ai.timefold.solver.core.impl.util.ConstantLambdaUtils;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class BiConstraintStubImpl<A, B> implements BiConstraintStub<A, B> {

    private final BiConstraintConstructor<A, B, ?> constraintConstructor;
    private final ScoreImpactType impactType;
    private @Nullable AbstractBiMatchWeight<A, B> matchWeight;

    public BiConstraintStubImpl(BiConstraintConstructor<A, B, ?> constraintConstructor, ScoreImpactType impactType) {
        this.constraintConstructor = Objects.requireNonNull(constraintConstructor);
        this.impactType = Objects.requireNonNull(impactType);
    }

    @Override
    public BiConstraintStub<A, B> withMatchWeight(ToIntBiFunction<A, B> matchWeigher) {
        failIfAlreadySet();
        matchWeight = new IntBiMatchWeight<>(matchWeigher);
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
    public BiConstraintStub<A, B> withLongMatchWeight(ToLongBiFunction<A, B> matchWeigher) {
        failIfAlreadySet();
        matchWeight = new LongBiMatchWeight<>(matchWeigher);
        return this;
    }

    @Override
    public BiConstraintStub<A, B> withBigDecimalMatchWeight(BiFunction<A, B, BigDecimal> matchWeigher) {
        failIfAlreadySet();
        matchWeight = new BigDecimalBiMatchWeight<>(matchWeigher);
        return this;
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    public <Score_ extends Score<Score_>> BiConstraintBuilder<A, B, Score_>
            usingDefaultConstraintWeight(Score_ constraintWeight) {
        var matchWeight_ = Objects.requireNonNullElseGet(matchWeight,
                () -> new IntBiMatchWeight<A, B>(ConstantLambdaUtils.biConstantOne()));
        return new BiConstraintBuilderImpl<>((BiConstraintConstructor<A, B, Score_>) constraintConstructor, impactType,
                matchWeight_, constraintWeight);
    }

}
