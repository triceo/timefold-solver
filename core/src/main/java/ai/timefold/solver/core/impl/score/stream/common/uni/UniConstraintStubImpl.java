package ai.timefold.solver.core.impl.score.stream.common.uni;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.uni.UniConstraintStub;
import ai.timefold.solver.core.impl.score.stream.common.ScoreImpactType;
import ai.timefold.solver.core.impl.util.ConstantLambdaUtils;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class UniConstraintStubImpl<A> implements UniConstraintStub<A> {

    private final UniConstraintConstructor<A, ?> constraintConstructor;
    private final ScoreImpactType impactType;
    private @Nullable AbstractUniMatchWeight<A> matchWeight;

    public UniConstraintStubImpl(UniConstraintConstructor<A, ?> constraintConstructor, ScoreImpactType impactType) {
        this.constraintConstructor = Objects.requireNonNull(constraintConstructor);
        this.impactType = Objects.requireNonNull(impactType);
    }

    @Override
    public UniConstraintStub<A> withMatchWeight(ToIntFunction<A> matchWeigher) {
        failIfAlreadySet();
        matchWeight = new IntUniMatchWeight<>(matchWeigher);
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
    public UniConstraintStub<A> withLongMatchWeight(ToLongFunction<A> matchWeigher) {
        failIfAlreadySet();
        matchWeight = new LongUniMatchWeight<>(matchWeigher);
        return this;
    }

    @Override
    public UniConstraintStub<A> withBigDecimalMatchWeight(Function<A, BigDecimal> matchWeigher) {
        failIfAlreadySet();
        matchWeight = new BigDecimalUniMatchWeight<>(matchWeigher);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <Score_ extends Score<Score_>> UniConstraintBuilderImpl<A, Score_>
            usingDefaultConstraintWeight(Score_ constraintWeight) {
        var matchWeight_ = Objects.requireNonNullElseGet(matchWeight,
                () -> new IntUniMatchWeight<A>(ConstantLambdaUtils.uniConstantOne()));
        return new UniConstraintBuilderImpl<>((UniConstraintConstructor<A, Score_>) constraintConstructor, impactType,
                matchWeight_, constraintWeight);
    }

}
