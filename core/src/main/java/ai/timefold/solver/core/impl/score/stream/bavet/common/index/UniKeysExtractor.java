package ai.timefold.solver.core.impl.score.stream.bavet.common.index;

import java.util.List;

import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.UniTuple;

@FunctionalInterface
public interface UniKeysExtractor<A> extends KeysExtractor<UniTuple<A>> {

    static <A> UniKeysExtractor<A> of(UniMappingFunction<A> keyFunction) {
        return tuple -> {
            var a = tuple.factA;
            return IndexKeys.of(keyFunction.apply(a));
        };
    }

    static <A> UniKeysExtractor<A> of(List<UniMappingFunction<A>> keyFunctionList) {
        var keyFunctionCount = keyFunctionList.size();
        return switch (keyFunctionCount) {
            case 1 -> of(keyFunctionList.get(0));
            case 2 -> {
                var keyFunction1 = keyFunctionList.get(0);
                var keyFunction2 = keyFunctionList.get(1);
                yield tuple -> {
                    var a = tuple.factA;
                    return IndexKeys.of(keyFunction1.apply(a), keyFunction2.apply(a));
                };
            }
            default -> tuple -> {
                var a = tuple.factA;
                var arr = new Object[keyFunctionCount];
                for (var i = 0; i < keyFunctionCount; i++) {
                    arr[i] = keyFunctionList.get(i).apply(a);
                }
                return IndexKeys.ofMany(arr);
            };
        };
    }

}
