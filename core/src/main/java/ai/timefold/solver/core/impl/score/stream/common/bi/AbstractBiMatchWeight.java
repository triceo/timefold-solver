package ai.timefold.solver.core.impl.score.stream.common.bi;

import java.util.function.BiFunction;
import java.util.function.ToIntBiFunction;
import java.util.function.ToLongBiFunction;

import ai.timefold.solver.core.impl.score.constraint.ConstraintMatchPolicy;
import ai.timefold.solver.core.impl.score.stream.common.MatchWeight;
import ai.timefold.solver.core.impl.score.stream.common.inliner.UndoScoreImpacter;
import ai.timefold.solver.core.impl.score.stream.common.inliner.WeightedScoreImpacter;

public sealed abstract class AbstractBiMatchWeight<A, B>
        implements MatchWeight
        permits BigDecimalBiMatchWeight, IntBiMatchWeight, LongBiMatchWeight {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <A, B> AbstractBiMatchWeight<A, B> of(Object weigher) {
        if (weigher instanceof ToIntBiFunction intWeigher) {
            return new IntBiMatchWeight<A, B>(intWeigher);
        } else if (weigher instanceof ToLongBiFunction longWeigher) {
            return new LongBiMatchWeight<A, B>(longWeigher);
        } else if (weigher instanceof BiFunction bigDecimalWeigher) {
            return new BigDecimalBiMatchWeight<A, B>(bigDecimalWeigher);
        } else {
            throw new IllegalArgumentException("Unsupported weigher type: " + weigher.getClass());
        }
    }

    @Override
    public final BiImpactFunction<A, B> createImpactFunction(ConstraintMatchPolicy constraintMatchPolicy) {
        return switch (constraintMatchPolicy) {
            case DISABLED -> this::naked;
            case ENABLED_WITHOUT_JUSTIFICATIONS -> this::partial;
            case ENABLED -> this::full;
        };
    }

    protected abstract UndoScoreImpacter naked(WeightedScoreImpacter<?, ?> impacter, A a, B b);

    protected abstract UndoScoreImpacter partial(WeightedScoreImpacter<?, ?> impacter, A a, B b);

    protected abstract UndoScoreImpacter full(WeightedScoreImpacter<?, ?> impacter, A a, B b);

}
