package ai.timefold.solver.core.impl.score.stream.bavet.common.index;

import java.util.List;

import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.TriTuple;

@FunctionalInterface
public interface TriKeysExtractor<A, B, C> extends KeysExtractor<TriTuple<A, B, C>> {

    static <A, B, C> TriKeysExtractor<A, B, C> of(TriMappingFunction<A, B, C> keyFunction) {
        return tuple -> {
            var a = tuple.factA;
            var b = tuple.factB;
            var c = tuple.factC;
            return IndexKeys.of(keyFunction.apply(a, b, c));
        };
    }

    static <A, B, C> TriKeysExtractor<A, B, C> of(List<TriMappingFunction<A, B, C>> keyFunctionList) {
        var keyFunctionCount = keyFunctionList.size();
        return switch (keyFunctionCount) {
            case 1 -> of(keyFunctionList.get(0));
            case 2 -> {
                var keyFunction1 = keyFunctionList.get(0);
                var keyFunction2 = keyFunctionList.get(1);
                yield tuple -> {
                    var a = tuple.factA;
                    var b = tuple.factB;
                    var c = tuple.factC;
                    return IndexKeys.of(keyFunction1.apply(a, b, c), keyFunction2.apply(a, b, c));
                };
            }
            default -> tuple -> {
                var a = tuple.factA;
                var b = tuple.factB;
                var c = tuple.factC;
                var arr = new Object[keyFunctionCount];
                for (var i = 0; i < keyFunctionCount; i++) {
                    arr[i] = keyFunctionList.get(i).apply(a, b, c);
                }
                return IndexKeys.ofMany(arr);
            };
        };
    }

}
