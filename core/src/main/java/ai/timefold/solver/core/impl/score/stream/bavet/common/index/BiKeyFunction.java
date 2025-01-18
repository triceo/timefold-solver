package ai.timefold.solver.core.impl.score.stream.bavet.common.index;

import java.util.List;
import java.util.Objects;

import ai.timefold.solver.core.api.function.TriFunction;
import ai.timefold.solver.core.impl.util.Pair;
import ai.timefold.solver.core.impl.util.Quadruple;
import ai.timefold.solver.core.impl.util.Triple;

record BiKeyFunction<A, B>(BiMappingFunction<A, B>... mappingFunctions)
        implements
            TriFunction<A, B, Object, Object>,
            KeyFunction {

    @SafeVarargs
    public BiKeyFunction {
    }

    @SuppressWarnings("unchecked")
    public BiKeyFunction(List<BiMappingFunction<A, B>> mappingFunctionList) {
        this(mappingFunctionList.toArray(new BiMappingFunction[0]));
    }

    public Object apply(A a, B b, Object oldKey) {
        var nonNullOldKey = oldKey != null;
        return switch (mappingFunctions.length) {
            case 1 -> mappingFunctions[0].apply(a, b);
            case 2 -> {
                var oldPair = nonNullOldKey ? (Pair<Object, Object>) oldKey : null;
                var subkey1 = mappingFunctions[0].apply(a, b);
                var firstSubKeyEqual = nonNullOldKey && subkey1.equals(oldPair.key());
                var subkey2 = mappingFunctions[1].apply(a, b);
                var subKeysEqual = firstSubKeyEqual && subkey2.equals(oldPair.value());
                if (subKeysEqual) {
                    yield oldPair;
                } else {
                    yield new Pair<>(subkey1, subkey2);
                }
            }
            case 3 -> {
                var oldTriple = nonNullOldKey ? (Triple<Object, Object, Object>) oldKey : null;
                var subkey1 = mappingFunctions[0].apply(a, b);
                var subKey1Equal = nonNullOldKey && subkey1.equals(oldTriple.a());
                var subkey2 = mappingFunctions[1].apply(a, b);
                var subKey2Equal = subKey1Equal && subkey2.equals(oldTriple.b());
                var subkey3 = mappingFunctions[2].apply(a, b);
                var subKeysEqual = subKey2Equal && subkey3.equals(oldTriple.c());
                if (subKeysEqual) {
                    yield oldTriple;
                } else {
                    yield new Triple<>(subkey1, subkey2, subkey3);
                }
            }
            case 4 -> {
                var oldQuadruple = nonNullOldKey ? (Quadruple<Object, Object, Object, Object>) oldKey : null;
                var subkey1 = mappingFunctions[0].apply(a, b);
                var subKey1Equal = nonNullOldKey && Objects.equals(subkey1, oldQuadruple.a());
                var subkey2 = mappingFunctions[1].apply(a, b);
                var subKey2Equal = subKey1Equal && Objects.equals(subkey2, oldQuadruple.b());
                var subkey3 = mappingFunctions[2].apply(a, b);
                var subKey3Equal = subKey2Equal && Objects.equals(subkey3, oldQuadruple.c());
                var subkey4 = mappingFunctions[3].apply(a, b);
                var subKeysEqual = subKey3Equal && Objects.equals(subkey4, oldQuadruple.d());
                if (subKeysEqual) {
                    yield oldQuadruple;
                } else {
                    yield new Quadruple<>(subkey1, subkey2, subkey3, subkey4);
                }
            }
            default -> {
                var oldArray = nonNullOldKey ? (Object[]) oldKey : null;
                var result = new Object[mappingFunctions.length];
                var subKeysEqual = nonNullOldKey;
                for (var i = 0; i < mappingFunctions.length; i++) {
                    var subkey = mappingFunctions[i].apply(a, b);
                    subKeysEqual = subKeysEqual && Objects.equals(subkey, oldArray[i]);
                    result[i] = subkey;
                }
                yield new IndexerKey(result);
            }
        };
    }
}
