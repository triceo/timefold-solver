package ai.timefold.solver.core.impl.score.stream.bavet.common.index;

import java.util.List;

import ai.timefold.solver.core.impl.util.Pair;
import ai.timefold.solver.core.impl.util.Quadruple;
import ai.timefold.solver.core.impl.util.Triple;

record UniKeyFunction<A>(UniMappingFunction<A>... mappingFunctions)
        implements
            UniMappingFunction<A>,
            KeyFunction<UniMappingFunction<A>> {

    @SafeVarargs
    public UniKeyFunction {
    }

    @SuppressWarnings("unchecked")
    public UniKeyFunction(List<UniMappingFunction<A>> mappingFunctionList) {
        this(mappingFunctionList.toArray(new UniMappingFunction[0]));
    }

    public Object apply(A a) {
        return switch (mappingFunctions.length) {
            case 1 -> mappingFunctions[0].apply(a);
            case 2 -> {
                var mapping1 = mappingFunctions[0];
                var mapping2 = mappingFunctions[1];
                yield new Pair<>(mapping1.apply(a), mapping2.apply(a));
            }
            case 3 -> {
                var mapping1 = mappingFunctions[0];
                var mapping2 = mappingFunctions[1];
                var mapping3 = mappingFunctions[2];
                yield new Triple<>(mapping1.apply(a), mapping2.apply(a), mapping3.apply(a));
            }
            case 4 -> {
                var mapping1 = mappingFunctions[0];
                var mapping2 = mappingFunctions[1];
                var mapping3 = mappingFunctions[2];
                var mapping4 = mappingFunctions[3];
                yield new Quadruple<>(mapping1.apply(a), mapping2.apply(a), mapping3.apply(a), mapping4.apply(a));
            }
            default -> {
                var result = new Object[mappingFunctions.length];
                for (var i = 0; i < mappingFunctions.length; i++) {
                    result[i] = mappingFunctions[i].apply(a);
                }
                yield new IndexerKey(result);
            }
        };
    }
}
