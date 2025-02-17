package ai.timefold.solver.core.impl.score.stream.common.bi;

import ai.timefold.solver.core.api.function.TriFunction;
import ai.timefold.solver.core.impl.score.stream.common.ImpactFunction;
import ai.timefold.solver.core.impl.score.stream.common.inliner.UndoScoreImpacter;
import ai.timefold.solver.core.impl.score.stream.common.inliner.WeightedScoreImpacter;

@FunctionalInterface
public interface BiImpactFunction<A, B> extends
        TriFunction<WeightedScoreImpacter<?, ?>, A, B, UndoScoreImpacter>,
        ImpactFunction {
}
