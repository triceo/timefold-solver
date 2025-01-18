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
                    var nonNullOldKey = oldKeys != null;
                    var oldIndexKeys = nonNullOldKey ? (IndexKeys) oldKeys : null;
                    var oldIndex0 = nonNullOldKey ? oldIndexKeys.get(0) : null;
                    var oldIndex1 = nonNullOldKey ? oldIndexKeys.get(1) : null;
                    var a = tuple.factA;
                    return IndexKeys.of(keyFunction1.apply(a, oldIndex0), keyFunction2.apply(a, oldIndex1));
                };
            }
            default -> (tuple, oldKeys) -> {
                var nonNullOldKey = oldKeys != null;
                var oldIndexKeys = nonNullOldKey ? (IndexKeys) oldKeys : null;
                var a = tuple.factA;
                var arr = new Object[keyFunctionCount];
                for (var i = 0; i < keyFunctionCount; i++) {
                    var oldIndexKey = nonNullOldKey ? oldIndexKeys.get(i) : null;
                    arr[i] = keyFunctionList.get(i).apply(a, oldIndexKey);
                }
                return IndexKeys.ofMany(arr);
            };
        };
    }

}
