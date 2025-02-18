package ai.timefold.solver.core.impl.score.stream.common.uni;

import java.util.Objects;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.uni.UniConstraintStub;
import ai.timefold.solver.core.impl.score.stream.common.ScoreImpactType;

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

    @SuppressWarnings("unchecked")
    @Override
    public <Score_ extends Score<Score_>> UniConstraintBuilderImpl<A, Score_>
            usingDefaultConstraintWeight(Score_ constraintWeight) {
        return new UniConstraintBuilderImpl<>((UniConstraintConstructor<A, Score_>) constraintConstructor, impactType,
                constraintWeight);
    }

}
