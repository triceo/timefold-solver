package ai.timefold.solver.core.impl.score.stream.bavet.common.index;

import java.util.List;

import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.QuadTuple;

@FunctionalInterface
public interface QuadKeysExtractor<A, B, C, D> extends KeysExtractor<QuadTuple<A, B, C, D>> {

    static <A, B, C, D> QuadKeysExtractor<A, B, C, D> of(QuadKeyFunction<A, B, C, D> keyFunction) {
        return (tuple, oldKeys) -> {
            var a = tuple.factA;
            var b = tuple.factB;
            var c = tuple.factC;
            var d = tuple.factD;
            return IndexKeys.of(keyFunction.apply(a, b, c, d, oldKeys));
        };
    }

    static <A, B, C, D> QuadKeysExtractor<A, B, C, D> of(List<QuadKeyFunction<A, B, C, D>> keyFunctionList) {
        var keyFunctionCount = keyFunctionList.size();
        return switch (keyFunctionCount) {
            case 1 -> of(keyFunctionList.get(0));
            case 2 -> {
                var keyFunction1 = keyFunctionList.get(0);
                var keyFunction2 = keyFunctionList.get(1);
                yield (tuple, oldKeys) -> {
                    var a = tuple.factA;
                    var b = tuple.factB;
                    var c = tuple.factC;
                    var d = tuple.factD;
                    if (oldKeys == null) {
                        return IndexKeys.of(keyFunction1.apply(a, b, c, d, null),
                                keyFunction2.apply(a, b, c, d, null));
                    }
                    var oldIndexKeys = (IndexKeys) oldKeys;
                    return IndexKeys.of(keyFunction1.apply(a, b, c, d, oldIndexKeys.get(0)),
                            keyFunction2.apply(a, b, c, d, oldIndexKeys.get(1)));
                };
            }
            default -> (tuple, oldKeys) -> {
                var a = tuple.factA;
                var b = tuple.factB;
                var c = tuple.factC;
                var d = tuple.factD;
                if (oldKeys == null) {
                    var arr = new Object[keyFunctionCount];
                    for (var i = 0; i < keyFunctionCount; i++) {
                        arr[i] = keyFunctionList.get(i).apply(a, b, c, d, null);
                    }
                    return IndexKeys.ofMany(arr);
                }
                var oldIndexKeys = (IndexKeys) oldKeys;
                var arr = new Object[keyFunctionCount];
                for (var i = 0; i < keyFunctionCount; i++) {
                    arr[i] = keyFunctionList.get(i).apply(a, b, c, d, oldIndexKeys.get(i));
                }
                return IndexKeys.ofMany(arr);
            };
        };
    }

}
