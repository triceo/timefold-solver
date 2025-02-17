package ai.timefold.solver.core.impl.score.stream.common;

import java.math.BigDecimal;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.impl.score.constraint.ConstraintMatchPolicy;
import ai.timefold.solver.core.impl.score.stream.common.inliner.ConstraintMatchSupplier;
import ai.timefold.solver.core.impl.score.stream.common.inliner.UndoScoreImpacter;
import ai.timefold.solver.core.impl.score.stream.common.inliner.WeightedScoreImpacter;

@FunctionalInterface
public interface MatchWeight {

    static <Score_ extends Score<Score_>> UndoScoreImpacter
            impactWithConstraintMatchNoJustifications(WeightedScoreImpacter<Score_, ?> impacter, int matchWeight) {
        var constraintMatchSupplier = ConstraintMatchSupplier.<Score_> empty();
        return impacter.impactScore(matchWeight, constraintMatchSupplier);
    }

    static <Score_ extends Score<Score_>> UndoScoreImpacter
            impactWithConstraintMatchNoJustifications(WeightedScoreImpacter<Score_, ?> impacter, long matchWeight) {
        var constraintMatchSupplier = ConstraintMatchSupplier.<Score_> empty();
        return impacter.impactScore(matchWeight, constraintMatchSupplier);
    }

    static <Score_ extends Score<Score_>> UndoScoreImpacter
            impactWithConstraintMatchNoJustifications(WeightedScoreImpacter<Score_, ?> impacter, BigDecimal matchWeight) {
        var constraintMatchSupplier = ConstraintMatchSupplier.<Score_> empty();
        return impacter.impactScore(matchWeight, constraintMatchSupplier);
    }

    ImpactFunction createImpactFunction(ConstraintMatchPolicy constraintMatchPolicy);

}
