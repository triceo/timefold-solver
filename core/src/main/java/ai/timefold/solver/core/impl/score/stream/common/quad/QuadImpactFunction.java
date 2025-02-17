package ai.timefold.solver.core.impl.score.stream.common.quad;

import ai.timefold.solver.core.api.function.PentaFunction;
import ai.timefold.solver.core.impl.score.stream.common.ImpactFunction;
import ai.timefold.solver.core.impl.score.stream.common.inliner.UndoScoreImpacter;
import ai.timefold.solver.core.impl.score.stream.common.inliner.WeightedScoreImpacter;

@FunctionalInterface
public interface QuadImpactFunction<A, B, C, D>
        extends PentaFunction<WeightedScoreImpacter<?, ?>, A, B, C, D, UndoScoreImpacter>,
        ImpactFunction {
}
