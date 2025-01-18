package ai.timefold.solver.core.impl.score.stream.bavet.common.index;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

import ai.timefold.solver.core.impl.util.Pair;
import ai.timefold.solver.core.impl.util.Quadruple;
import ai.timefold.solver.core.impl.util.Triple;

final class UniKeyFunction<A>
        implements BiFunction<A, Object, Object>, KeyFunction {

    private final int mappingFunctionCount;
    private final UniMappingFunction<A>[] mappingFunctions;
    private final UniMappingFunction<A> mappingFunction0;
    private final UniMappingFunction<A> mappingFunction1;
    private final UniMappingFunction<A> mappingFunction2;
    private final UniMappingFunction<A> mappingFunction3;
    private final BiFunction<A, Object, Object> path;

    public UniKeyFunction(UniMappingFunction<A> mappingFunction) {
        this(Collections.singletonList(mappingFunction));
    }

    @SuppressWarnings("unchecked")
    public UniKeyFunction(List<UniMappingFunction<A>> mappingFunctionList) {
        this.mappingFunctionCount = mappingFunctionList.size();
        this.mappingFunctions = mappingFunctionList.toArray(new UniMappingFunction[0]);
        this.mappingFunction0 = mappingFunctions[0];
        this.mappingFunction1 = mappingFunctionCount > 1 ? mappingFunctions[1] : null;
        this.mappingFunction2 = mappingFunctionCount > 2 ? mappingFunctions[2] : null;
        this.mappingFunction3 = mappingFunctionCount > 3 ? mappingFunctions[3] : null;
        this.path = switch (mappingFunctionCount) {
            case 1 -> this::apply1;
            case 2 -> this::apply2;
            case 3 -> this::apply3;
            case 4 -> this::apply4;
            default -> this::applyMany;
        };
    }

    @Override
    public Object apply(A a, Object oldKey) {
        return path.apply(a, oldKey);
    }

    private Object apply1(A a, Object oldKey) {
        return mappingFunction0.apply(a);
    }

    @SuppressWarnings("unchecked")
    private Object apply2(A a, Object oldKey) {
        var subkey1 = mappingFunction0.apply(a);
        var subkey2 = mappingFunction1.apply(a);
        if (oldKey == null) {
            return new Pair<>(subkey1, subkey2);
        }
        var oldPair = (Pair<Object, Object>) oldKey;
        var subKey1Equal = Objects.equals(subkey1, oldPair.key());
        var subKeysEqual = subKey1Equal && Objects.equals(subkey2, oldPair.value());
        if (subKeysEqual) {
            return oldPair;
        } else {
            return new Pair<>(subkey1, subkey2);
        }
    }

    @SuppressWarnings("unchecked")
    private Object apply3(A a, Object oldKey) {
        var subkey1 = mappingFunction0.apply(a);
        var subkey2 = mappingFunction1.apply(a);
        var subkey3 = mappingFunction2.apply(a);
        if (oldKey == null) {
            return new Triple<>(subkey1, subkey2, subkey3);
        }
        var oldTriple = (Triple<Object, Object, Object>) oldKey;
        var subKey1Equal = Objects.equals(subkey1, oldTriple.a());
        var subKey2Equal = subKey1Equal && Objects.equals(subkey2, oldTriple.b());
        var subKeysEqual = subKey2Equal && Objects.equals(subkey3, oldTriple.c());
        if (subKeysEqual) {
            return oldTriple;
        } else {
            return new Triple<>(subkey1, subkey2, subkey3);
        }
    }

    @SuppressWarnings("unchecked")
    private Object apply4(A a, Object oldKey) {
        var subkey1 = mappingFunction0.apply(a);
        var subkey2 = mappingFunction1.apply(a);
        var subkey3 = mappingFunction2.apply(a);
        var subkey4 = mappingFunction3.apply(a);
        if (oldKey == null) {
            return new Quadruple<>(subkey1, subkey2, subkey3, subkey4);
        }
        var oldQuadruple = (Quadruple<Object, Object, Object, Object>) oldKey;
        var subKey1Equal = Objects.equals(subkey1, oldQuadruple.a());
        var subKey2Equal = subKey1Equal && Objects.equals(subkey2, oldQuadruple.b());
        var subKey3Equal = subKey2Equal && Objects.equals(subkey3, oldQuadruple.c());
        var subKeysEqual = subKey3Equal && Objects.equals(subkey4, oldQuadruple.d());
        if (subKeysEqual) {
            return oldQuadruple;
        } else {
            return new Quadruple<>(subkey1, subkey2, subkey3, subkey4);
        }
    }

    private Object applyMany(A a, Object oldKey) {
        var result = new Object[mappingFunctionCount];
        if (oldKey == null) {
            for (var i = 0; i < mappingFunctionCount; i++) {
                result[i] = mappingFunctions[i].apply(a);
            }
        } else {
            var oldArray = (Object[]) oldKey;
            var subKeysEqual = true;
            for (var i = 0; i < mappingFunctionCount; i++) {
                var subkey = mappingFunctions[i].apply(a);
                subKeysEqual = subKeysEqual && Objects.equals(subkey, oldArray[i]);
                result[i] = subkey;
            }
            if (subKeysEqual) {
                return oldKey;
            }
        }
        return new IndexerKey(result);
    }

}
