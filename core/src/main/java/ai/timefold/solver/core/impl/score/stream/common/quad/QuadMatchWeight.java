package ai.timefold.solver.core.impl.score.stream.common.quad;

import ai.timefold.solver.core.api.function.QuadFunction;
import ai.timefold.solver.core.api.function.ToIntQuadFunction;
import ai.timefold.solver.core.api.function.ToLongQuadFunction;
import ai.timefold.solver.core.impl.score.constraint.ConstraintMatchPolicy;
import ai.timefold.solver.core.impl.score.stream.common.MatchWeight;
import ai.timefold.solver.core.impl.score.stream.common.inliner.UndoScoreImpacter;
import ai.timefold.solver.core.impl.score.stream.common.inliner.WeightedScoreImpacter;

public sealed abstract class QuadMatchWeight<A, B, C, D>
        implements MatchWeight
        permits BigDecimalQuadMatchWeight, IntQuadMatchWeight, LongQuadMatchWeight {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <A, B, C, D> QuadMatchWeight<A, B, C, D> of(Object weigher) {
        if (weigher instanceof ToIntQuadFunction intWeigher) {
            return new IntQuadMatchWeight<A, B, C, D>(intWeigher);
        } else if (weigher instanceof ToLongQuadFunction longWeigher) {
            return new LongQuadMatchWeight<A, B, C, D>(longWeigher);
        } else if (weigher instanceof QuadFunction bigDecimalWeigher) {
            return new BigDecimalQuadMatchWeight<A, B, C, D>(bigDecimalWeigher);
        } else {
            throw new IllegalArgumentException("Unsupported weigher type: " + weigher.getClass());
        }
    }

    @Override
    public final QuadImpactFunction<A, B, C, D> createImpactFunction(ConstraintMatchPolicy constraintMatchPolicy) {
        return switch (constraintMatchPolicy) {
            case DISABLED -> this::naked;
            case ENABLED_WITHOUT_JUSTIFICATIONS -> this::partial;
            case ENABLED -> this::full;
        };
    }

    protected abstract UndoScoreImpacter naked(WeightedScoreImpacter<?, ?> impacter, A a, B b, C c, D d);

    protected abstract UndoScoreImpacter partial(WeightedScoreImpacter<?, ?> impacter, A a, B b, C c, D d);

    protected abstract UndoScoreImpacter full(WeightedScoreImpacter<?, ?> impacter, A a, B b, C c, D d);

}
