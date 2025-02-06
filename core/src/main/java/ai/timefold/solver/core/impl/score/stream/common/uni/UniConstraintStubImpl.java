package ai.timefold.solver.core.impl.score.stream.common.uni;

import java.util.Objects;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.tmp.UniConstraintStub;
import ai.timefold.solver.core.impl.score.stream.common.ScoreImpactType;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class UniConstraintStubImpl<A, Score_ extends Score<Score_>>
        implements UniConstraintStub<A, Score_> {

    private final UniConstraintConstructor<A, Score_> constraintConstructor;
    private final ScoreImpactType impactType;

    public UniConstraintStubImpl(UniConstraintConstructor<A, Score_> constraintConstructor, ScoreImpactType impactType) {
        this.constraintConstructor = Objects.requireNonNull(constraintConstructor);
        this.impactType = Objects.requireNonNull(impactType);
    }

    @Override
    public @NonNull UniConstraintBuilderImpl<A, Score_> usingDefaultConstraintWeight(@Nullable Score_ constraintWeight) {
        return new UniConstraintBuilderImpl<>(constraintConstructor, impactType, constraintWeight);
    }

}
