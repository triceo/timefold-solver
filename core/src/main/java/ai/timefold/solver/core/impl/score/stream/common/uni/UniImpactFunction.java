package ai.timefold.solver.core.impl.score.stream.common.uni;

import java.util.function.BiFunction;

import ai.timefold.solver.core.impl.score.stream.common.ImpactFunction;
import ai.timefold.solver.core.impl.score.stream.common.inliner.UndoScoreImpacter;
import ai.timefold.solver.core.impl.score.stream.common.inliner.WeightedScoreImpacter;

@FunctionalInterface
public interface UniImpactFunction<A>
        extends BiFunction<WeightedScoreImpacter<?, ?>, A, UndoScoreImpacter>, ImpactFunction {
}
