package ai.timefold.solver.core.impl.score.stream.bavet.common.index;

import java.util.List;

import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.BiTuple;

@FunctionalInterface
public interface BiKeysExtractor<A, B> extends KeysExtractor<BiTuple<A, B>> {

    static <A, B> BiKeysExtractor<A, B> of(BiMappingFunction<A, B> keyFunction) {
        return tuple -> {
            var a = tuple.factA;
            var b = tuple.factB;
            return IndexKeys.of(keyFunction.apply(a, b));
        };
    }

    static <A, B> BiKeysExtractor<A, B> of(List<BiMappingFunction<A, B>> keyFunctionList) {
        var keyFunctionCount = keyFunctionList.size();
        return switch (keyFunctionCount) {
            case 1 -> of(keyFunctionList.get(0));
            case 2 -> {
                var keyFunction1 = keyFunctionList.get(0);
                var keyFunction2 = keyFunctionList.get(1);
                yield tuple -> {
                    var a = tuple.factA;
                    var b = tuple.factB;
                    return IndexKeys.of(keyFunction1.apply(a, b), keyFunction2.apply(a, b));
                };
            }
            default -> tuple -> {
                var a = tuple.factA;
                var b = tuple.factB;
                var arr = new Object[keyFunctionCount];
                for (var i = 0; i < keyFunctionCount; i++) {
                    arr[i] = keyFunctionList.get(i).apply(a, b);
                }
                return IndexKeys.ofMany(arr);
            };
        };
    }

}
