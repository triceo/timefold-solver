package ai.timefold.solver.core.impl.score.stream.bavet.common.index;

import ai.timefold.solver.core.api.function.QuadFunction;
import ai.timefold.solver.core.impl.score.stream.common.AbstractJoiner;
import ai.timefold.solver.core.impl.score.stream.common.penta.DefaultPentaJoiner;

@FunctionalInterface
interface QuadMappingFunction<A, B, C, D> extends QuadFunction<A, B, C, D, Object> {

    static <A, B, C, D> QuadMappingFunction<A, B, C, D> of(AbstractJoiner<?> joiner, int index) {
        var castJoiner = (DefaultPentaJoiner<A, B, C, D, ?>) joiner;
        return (a, b, c, d) -> castJoiner.getLeftMapping(index).apply(a, b, c, d);
    }

}
