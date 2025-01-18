package ai.timefold.solver.core.impl.score.stream.bavet.common.index;

import java.util.List;

import ai.timefold.solver.core.impl.util.Pair;
import ai.timefold.solver.core.impl.util.Quadruple;
import ai.timefold.solver.core.impl.util.Triple;

record TriKeyFunction<A, B, C>(TriMappingFunction<A, B, C>... mappingFunctions)
        implements
            TriMappingFunction<A, B, C>,
            KeyFunction<TriMappingFunction<A, B, C>> {

    @SafeVarargs
    public TriKeyFunction {
    }

    @SuppressWarnings("unchecked")
    public TriKeyFunction(List<TriMappingFunction<A, B, C>> mappingFunctionList) {
        this(mappingFunctionList.toArray(new TriMappingFunction[0]));
    }

    public Object apply(A a, B b, C c) {
        return switch (mappingFunctions.length) {
            case 1 -> mappingFunctions[0].apply(a, b, c);
            case 2 -> {
                var mapping1 = mappingFunctions[0];
                var mapping2 = mappingFunctions[1];
                yield new Pair<>(mapping1.apply(a, b, c), mapping2.apply(a, b, c));
            }
            case 3 -> {
                var mapping1 = mappingFunctions[0];
                var mapping2 = mappingFunctions[1];
                var mapping3 = mappingFunctions[2];
                yield new Triple<>(mapping1.apply(a, b, c), mapping2.apply(a, b, c), mapping3.apply(a, b, c));
            }
            case 4 -> {
                var mapping1 = mappingFunctions[0];
                var mapping2 = mappingFunctions[1];
                var mapping3 = mappingFunctions[2];
                var mapping4 = mappingFunctions[3];
                yield new Quadruple<>(mapping1.apply(a, b, c), mapping2.apply(a, b, c), mapping3.apply(a, b, c),
                        mapping4.apply(a, b, c));
            }
            default -> {
                var result = new Object[mappingFunctions.length];
                for (var i = 0; i < mappingFunctions.length; i++) {
                    result[i] = mappingFunctions[i].apply(a, b, c);
                }
                yield new IndexerKey(result);
            }
        };
    }
}
