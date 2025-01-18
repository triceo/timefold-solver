package ai.timefold.solver.core.impl.score.stream.bavet.common.index;

import java.util.List;

import ai.timefold.solver.core.impl.util.Pair;
import ai.timefold.solver.core.impl.util.Quadruple;
import ai.timefold.solver.core.impl.util.Triple;

record QuadKeyFunction<A, B, C, D>(QuadMappingFunction<A, B, C, D>... mappingFunctions)
        implements
            QuadMappingFunction<A, B, C, D>,
            KeyFunction<QuadMappingFunction<A, B, C, D>> {

    @SafeVarargs
    public QuadKeyFunction {
    }

    @SuppressWarnings("unchecked")
    public QuadKeyFunction(List<QuadMappingFunction<A, B, C, D>> mappingFunctionList) {
        this(mappingFunctionList.toArray(new QuadMappingFunction[0]));
    }

    public Object apply(A a, B b, C c, D d) {
        return switch (mappingFunctions.length) {
            case 1 -> mappingFunctions[0].apply(a, b, c, d);
            case 2 -> {
                var mapping1 = mappingFunctions[0];
                var mapping2 = mappingFunctions[1];
                yield new Pair<>(mapping1.apply(a, b, c, d), mapping2.apply(a, b, c, d));
            }
            case 3 -> {
                var mapping1 = mappingFunctions[0];
                var mapping2 = mappingFunctions[1];
                var mapping3 = mappingFunctions[2];
                yield new Triple<>(mapping1.apply(a, b, c, d), mapping2.apply(a, b, c, d), mapping3.apply(a, b, c, d));
            }
            case 4 -> {
                var mapping1 = mappingFunctions[0];
                var mapping2 = mappingFunctions[1];
                var mapping3 = mappingFunctions[2];
                var mapping4 = mappingFunctions[3];
                yield new Quadruple<>(mapping1.apply(a, b, c, d), mapping2.apply(a, b, c, d), mapping3.apply(a, b, c, d),
                        mapping4.apply(a, b, c, d));
            }
            default -> {
                var result = new Object[mappingFunctions.length];
                for (var i = 0; i < mappingFunctions.length; i++) {
                    result[i] = mappingFunctions[i].apply(a, b, c, d);
                }
                yield new IndexerKey(result);
            }
        };
    }
}
