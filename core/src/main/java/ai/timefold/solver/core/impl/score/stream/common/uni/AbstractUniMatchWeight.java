package ai.timefold.solver.core.impl.score.stream.common.uni;

import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import ai.timefold.solver.core.impl.score.constraint.ConstraintMatchPolicy;
import ai.timefold.solver.core.impl.score.stream.common.MatchWeight;
import ai.timefold.solver.core.impl.score.stream.common.inliner.UndoScoreImpacter;
import ai.timefold.solver.core.impl.score.stream.common.inliner.WeightedScoreImpacter;

public sealed abstract class AbstractUniMatchWeight<A>
        implements MatchWeight
        permits BigDecimalUniMatchWeight, IntUniMatchWeight, LongUniMatchWeight {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <A> AbstractUniMatchWeight<A> of(Object weigher) {
        if (weigher instanceof ToIntFunction intWeigher) {
            return new IntUniMatchWeight<A>(intWeigher);
        } else if (weigher instanceof ToLongFunction longWeigher) {
            return new LongUniMatchWeight<A>(longWeigher);
        } else if (weigher instanceof Function bigDecimalWeigher) {
            return new BigDecimalUniMatchWeight<A>(bigDecimalWeigher);
        } else {
            throw new IllegalArgumentException("Unsupported weigher type: " + weigher.getClass());
        }
    }

    @Override
    public final UniImpactFunction<A> createImpactFunction(ConstraintMatchPolicy constraintMatchPolicy) {
        return switch (constraintMatchPolicy) {
            case DISABLED -> this::naked;
            case ENABLED_WITHOUT_JUSTIFICATIONS -> this::partial;
            case ENABLED -> this::full;
        };
    }

    protected abstract UndoScoreImpacter naked(WeightedScoreImpacter<?, ?> impacter, A a);

    protected abstract UndoScoreImpacter partial(WeightedScoreImpacter<?, ?> impacter, A a);

    protected abstract UndoScoreImpacter full(WeightedScoreImpacter<?, ?> impacter, A a);

}
