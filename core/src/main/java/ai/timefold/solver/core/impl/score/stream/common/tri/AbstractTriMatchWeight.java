package ai.timefold.solver.core.impl.score.stream.common.tri;

import ai.timefold.solver.core.api.function.ToIntTriFunction;
import ai.timefold.solver.core.api.function.ToLongTriFunction;
import ai.timefold.solver.core.api.function.TriFunction;
import ai.timefold.solver.core.impl.score.constraint.ConstraintMatchPolicy;
import ai.timefold.solver.core.impl.score.stream.common.MatchWeight;
import ai.timefold.solver.core.impl.score.stream.common.inliner.UndoScoreImpacter;
import ai.timefold.solver.core.impl.score.stream.common.inliner.WeightedScoreImpacter;

public sealed abstract class AbstractTriMatchWeight<A, B, C>
        implements MatchWeight
        permits BigDecimalTriMatchWeight, IntTriMatchWeight, LongTriMatchWeight {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <A, B, C> AbstractTriMatchWeight<A, B, C> of(Object weigher) {
        if (weigher instanceof ToIntTriFunction intWeigher) {
            return new IntTriMatchWeight<A, B, C>(intWeigher);
        } else if (weigher instanceof ToLongTriFunction longWeigher) {
            return new LongTriMatchWeight<A, B, C>(longWeigher);
        } else if (weigher instanceof TriFunction bigDecimalWeigher) {
            return new BigDecimalTriMatchWeight<A, B, C>(bigDecimalWeigher);
        } else {
            throw new IllegalArgumentException("Unsupported weigher type: " + weigher.getClass());
        }
    }

    @Override
    public final TriImpactFunction<A, B, C> createImpactFunction(ConstraintMatchPolicy constraintMatchPolicy) {
        return switch (constraintMatchPolicy) {
            case DISABLED -> this::naked;
            case ENABLED_WITHOUT_JUSTIFICATIONS -> this::partial;
            case ENABLED -> this::full;
        };
    }

    protected abstract UndoScoreImpacter naked(WeightedScoreImpacter<?, ?> impacter, A a, B b, C c);

    protected abstract UndoScoreImpacter partial(WeightedScoreImpacter<?, ?> impacter, A a, B b, C c);

    protected abstract UndoScoreImpacter full(WeightedScoreImpacter<?, ?> impacter, A a, B b, C c);

}
