package ai.timefold.solver.core.impl.score.stream.common.bi;

import java.util.Objects;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.bi.BiConstraintBuilder;
import ai.timefold.solver.core.api.score.stream.bi.BiConstraintStub;
import ai.timefold.solver.core.impl.score.stream.common.ScoreImpactType;

import org.jspecify.annotations.NullMarked;

@NullMarked
public final class BiConstraintStubImpl<A, B, Score_ extends Score<Score_>>
        implements BiConstraintStub<A, B, Score_> {

    private final BiConstraintConstructor<A, B, Score_> constraintConstructor;
    private final ScoreImpactType impactType;

    public BiConstraintStubImpl(BiConstraintConstructor<A, B, Score_> constraintConstructor, ScoreImpactType impactType) {
        this.constraintConstructor = Objects.requireNonNull(constraintConstructor);
        this.impactType = Objects.requireNonNull(impactType);
    }

    @Override
    public BiConstraintBuilder<A, B, Score_> usingDefaultConstraintWeight(Score_ constraintWeight) {
        return new BiConstraintBuilderImpl<>(constraintConstructor, impactType, constraintWeight);
    }

}
