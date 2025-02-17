package ai.timefold.solver.core.impl.score.stream.common;

import java.util.Objects;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintBuilder;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("rawtypes")
public abstract class AbstractConstraintBuilder<Score_ extends Score<Score_>> implements ConstraintBuilder {

    private final ConstraintConstructor constraintConstructor;
    private final ScoreImpactType impactType;
    private final MatchWeight matchWeight;
    private final Score_ constraintWeight;

    protected AbstractConstraintBuilder(ConstraintConstructor constraintConstructor, ScoreImpactType impactType,
            MatchWeight matchWeight, Score_ constraintWeight) {
        this.constraintConstructor = Objects.requireNonNull(constraintConstructor);
        this.impactType = Objects.requireNonNull(impactType);
        this.matchWeight = Objects.requireNonNull(matchWeight);
        this.constraintWeight = constraintWeight;
    }

    protected abstract <JustificationMapping_> JustificationMapping_ getJustificationMapping();

    protected abstract <IndictedObjectsMapping_> IndictedObjectsMapping_ getIndictedObjectsMapping();

    @SuppressWarnings("unchecked")
    @Override
    public final @NonNull Constraint asConstraintDescribed(@NonNull String constraintName,
            @NonNull String constraintDescription, @NonNull String constraintGroup) {
        return constraintConstructor.apply(null, constraintName, constraintDescription, constraintGroup, constraintWeight,
                matchWeight, impactType, getJustificationMapping(), getIndictedObjectsMapping());
    }

    @SuppressWarnings("unchecked")
    @Override
    public final Constraint asConstraint(@Nullable String constraintPackage, @Nullable String constraintName) {
        return constraintConstructor.apply(constraintPackage, constraintName, "", Constraint.DEFAULT_CONSTRAINT_GROUP,
                constraintWeight, matchWeight, impactType, getJustificationMapping(), getIndictedObjectsMapping());
    }

}
