package ai.timefold.solver.core.impl.score.stream.bavet.common.index;

import java.util.List;

import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.UniTuple;

@FunctionalInterface
public interface UniKeysExtractor<A> extends KeysExtractor<UniTuple<A>> {

    static <A> UniKeysExtractor<A> of(UniKeyFunction<A> keyFunction) {
        return (tuple, oldKeys) -> {
            var a = tuple.factA;
            return IndexKeys.of(keyFunction.apply(a, oldKeys));
        };
    }

    static <A> UniKeysExtractor<A> of(List<UniKeyFunction<A>> keyFunctionList) {
        var keyFunctionCount = keyFunctionList.size();
        return switch (keyFunctionCount) {
            case 1 -> of(keyFunctionList.get(0));
            case 2 -> {
                var keyFunction1 = keyFunctionList.get(0);
                var keyFunction2 = keyFunctionList.get(1);
                yield (tuple, oldKeys) -> {
                    var a = tuple.factA;
                    if (oldKeys == null) {
                        return IndexKeys.of(keyFunction1.apply(a, null),
                                keyFunction2.apply(a, null));
                    }
                    var oldIndexKeys = (IndexKeys) oldKeys;
                    return IndexKeys.of(keyFunction1.apply(a, oldIndexKeys.get(0)),
                            keyFunction2.apply(a, oldIndexKeys.get(1)));
                };
            }
            default -> (tuple, oldKeys) -> {
                var a = tuple.factA;
                if (oldKeys == null) {
                    var arr = new Object[keyFunctionCount];
                    for (var i = 0; i < keyFunctionCount; i++) {
                        arr[i] = keyFunctionList.get(i).apply(a, null);
                    }
                    return IndexKeys.ofMany(arr);
                }
                var oldIndexKeys = (IndexKeys) oldKeys;
                var arr = new Object[keyFunctionCount];
                for (var i = 0; i < keyFunctionCount; i++) {
                    arr[i] = keyFunctionList.get(i).apply(a, oldIndexKeys.get(i));
                }
                return IndexKeys.ofMany(arr);
            };
        };
    }

}
