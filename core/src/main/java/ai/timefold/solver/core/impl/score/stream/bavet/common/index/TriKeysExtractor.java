package ai.timefold.solver.core.impl.score.stream.bavet.common.index;

import java.util.List;

import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.TriTuple;

@FunctionalInterface
public interface TriKeysExtractor<A, B, C> extends KeysExtractor<TriTuple<A, B, C>> {

    static <A, B, C> TriKeysExtractor<A, B, C> of(TriKeyFunction<A, B, C> keyFunction) {
        return (tuple, oldKeys) -> {
            var a = tuple.factA;
            var b = tuple.factB;
            var c = tuple.factC;
            return IndexKeys.of(keyFunction.apply(a, b, c, oldKeys));
        };
    }

    static <A, B, C> TriKeysExtractor<A, B, C> of(List<TriKeyFunction<A, B, C>> keyFunctionList) {
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
                    if (oldKeys == null) {
                        return IndexKeys.of(keyFunction1.apply(a, b, c, null),
                                keyFunction2.apply(a, b, c, null));
                    }
                    var oldIndexKeys = (IndexKeys) oldKeys;
                    return IndexKeys.of(keyFunction1.apply(a, b, c, oldIndexKeys.get(0)),
                            keyFunction2.apply(a, b, c, oldIndexKeys.get(1)));
                };
            }
            default -> (tuple, oldKeys) -> {
                var a = tuple.factA;
                var b = tuple.factB;
                var c = tuple.factC;
                if (oldKeys == null) {
                    var arr = new Object[keyFunctionCount];
                    for (var i = 0; i < keyFunctionCount; i++) {
                        arr[i] = keyFunctionList.get(i).apply(a, b, c, null);
                    }
                    return IndexKeys.ofMany(arr);
                }
                var oldIndexKeys = (IndexKeys) oldKeys;
                var arr = new Object[keyFunctionCount];
                for (var i = 0; i < keyFunctionCount; i++) {
                    arr[i] = keyFunctionList.get(i).apply(a, b, c, oldIndexKeys.get(i));
                }
                return IndexKeys.ofMany(arr);
            };
        };
    }

}
