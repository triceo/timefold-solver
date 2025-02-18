package ai.timefold.solver.core.impl.score.stream.common.bi;

import java.util.Objects;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.bi.BiConstraintBuilder;
import ai.timefold.solver.core.api.score.stream.bi.BiConstraintStub;
import ai.timefold.solver.core.impl.score.stream.common.ScoreImpactType;

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

    @SuppressWarnings({ "unchecked" })
    @Override
    public <Score_ extends Score<Score_>> BiConstraintBuilder<A, B, Score_>
            usingDefaultConstraintWeight(Score_ constraintWeight) {
        return new BiConstraintBuilderImpl<>((BiConstraintConstructor<A, B, Score_>) constraintConstructor, impactType,
                constraintWeight);
    }

}
