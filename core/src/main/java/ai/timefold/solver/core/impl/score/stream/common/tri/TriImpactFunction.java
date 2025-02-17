package ai.timefold.solver.core.impl.score.stream.common.tri;

import ai.timefold.solver.core.api.function.QuadFunction;
import ai.timefold.solver.core.impl.score.stream.common.ImpactFunction;
import ai.timefold.solver.core.impl.score.stream.common.inliner.UndoScoreImpacter;
import ai.timefold.solver.core.impl.score.stream.common.inliner.WeightedScoreImpacter;

@FunctionalInterface
public interface TriImpactFunction<A, B, C>
        extends QuadFunction<WeightedScoreImpacter<?, ?>, A, B, C, UndoScoreImpacter>,
        ImpactFunction {
}
