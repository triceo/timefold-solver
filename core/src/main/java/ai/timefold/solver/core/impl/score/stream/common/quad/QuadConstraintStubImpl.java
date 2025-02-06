package ai.timefold.solver.core.impl.score.stream.common.quad;

import java.util.Objects;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.quad.QuadConstraintBuilder;
import ai.timefold.solver.core.api.score.stream.tmp.QuadConstraintStub;
import ai.timefold.solver.core.impl.score.stream.common.ScoreImpactType;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class QuadConstraintStubImpl<A, B, C, D, Score_ extends Score<Score_>>
        implements QuadConstraintStub<A, B, C, D, Score_> {

    private final QuadConstraintConstructor<A, B, C, D, Score_> constraintConstructor;
    private final ScoreImpactType impactType;

    public QuadConstraintStubImpl(QuadConstraintConstructor<A, B, C, D, Score_> constraintConstructor, ScoreImpactType impactType) {
        this.constraintConstructor = Objects.requireNonNull(constraintConstructor);
        this.impactType = Objects.requireNonNull(impactType);
    }

    @Override
    public @NonNull QuadConstraintBuilder<A, B, C, D, Score_> usingDefaultConstraintWeight(@Nullable Score_ constraintWeight) {
        return new QuadConstraintBuilderImpl<>(constraintConstructor, impactType, constraintWeight);
    }

}
