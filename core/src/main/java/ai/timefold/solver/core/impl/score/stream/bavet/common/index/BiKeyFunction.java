package ai.timefold.solver.core.impl.score.stream.bavet.common.index;

import java.util.List;

import ai.timefold.solver.core.impl.util.Pair;
import ai.timefold.solver.core.impl.util.Quadruple;
import ai.timefold.solver.core.impl.util.Triple;

record BiKeyFunction<A, B>(BiMappingFunction<A, B>... mappingFunctions)
        implements
            BiMappingFunction<A, B>,
            KeyFunction<BiMappingFunction<A, B>> {

    @SafeVarargs
    public BiKeyFunction {
    }

    @SuppressWarnings("unchecked")
    public BiKeyFunction(List<BiMappingFunction<A, B>> mappingFunctionList) {
        this(mappingFunctionList.toArray(new BiMappingFunction[0]));
    }

    public Object apply(A a, B b) {
        var x = switch (mappingFunctions.length) {
            case 1 -> mappingFunctions[0].apply(a, b);
            case 2 -> {
                var mapping1 = mappingFunctions[0];
                var mapping2 = mappingFunctions[1];
                yield new Pair<>(mapping1.apply(a, b), mapping2.apply(a, b));
            }
            case 3 -> {
                var mapping1 = mappingFunctions[0];
                var mapping2 = mappingFunctions[1];
                var mapping3 = mappingFunctions[2];
                yield new Triple<>(mapping1.apply(a, b), mapping2.apply(a, b), mapping3.apply(a, b));
            }
            case 4 -> {
                var mapping1 = mappingFunctions[0];
                var mapping2 = mappingFunctions[1];
                var mapping3 = mappingFunctions[2];
                var mapping4 = mappingFunctions[3];
                yield new Quadruple<>(mapping1.apply(a, b), mapping2.apply(a, b), mapping3.apply(a, b), mapping4.apply(a, b));
            }
            default -> {
                var result = new Object[mappingFunctions.length];
                for (var i = 0; i < mappingFunctions.length; i++) {
                    result[i] = mappingFunctions[i].apply(a, b);
                }
                yield new IndexerKey(result);
            }
        };
        System.out.println(a + " " + b + " " + x);
        return x;
    }
}
