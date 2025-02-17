package ai.timefold.solver.core.impl.score.stream.common.tri;

import java.util.Objects;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.tri.TriConstraintBuilder;
import ai.timefold.solver.core.api.score.stream.tri.TriConstraintStub;
import ai.timefold.solver.core.impl.score.stream.common.ScoreImpactType;

import org.jspecify.annotations.NullMarked;

@NullMarked
public final class TriConstraintStubImpl<A, B, C> implements TriConstraintStub<A, B, C> {

    private final TriConstraintConstructor<A, B, C, ?> constraintConstructor;
    private final ScoreImpactType impactType;

    public TriConstraintStubImpl(TriConstraintConstructor<A, B, C, ?> constraintConstructor, ScoreImpactType impactType) {
        this.constraintConstructor = Objects.requireNonNull(constraintConstructor);
        this.impactType = Objects.requireNonNull(impactType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <Score_ extends Score<Score_>> TriConstraintBuilder<A, B, C, Score_>
            usingDefaultConstraintWeight(Score_ constraintWeight) {
        return new TriConstraintBuilderImpl<>((TriConstraintConstructor<A, B, C, Score_>) constraintConstructor, impactType,
                constraintWeight);
    }

}
