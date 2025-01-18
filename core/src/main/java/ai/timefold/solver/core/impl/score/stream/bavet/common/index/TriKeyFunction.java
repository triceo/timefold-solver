package ai.timefold.solver.core.impl.score.stream.bavet.common.index;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ai.timefold.solver.core.api.function.QuadFunction;
import ai.timefold.solver.core.impl.util.Pair;
import ai.timefold.solver.core.impl.util.Quadruple;
import ai.timefold.solver.core.impl.util.Triple;

final class TriKeyFunction<A, B, C>
        implements QuadFunction<A, B, C, Object, Object>, KeyFunction {

    private final TriMappingFunction<A, B, C>[] mappingFunctions;
    private final TriMappingFunction<A, B, C> mappingFunction0;
    private final TriMappingFunction<A, B, C> mappingFunction1;
    private final TriMappingFunction<A, B, C> mappingFunction2;
    private final TriMappingFunction<A, B, C> mappingFunction3;
    private final QuadFunction<A, B, C, Object, Object> path;

    public TriKeyFunction(TriMappingFunction<A, B, C> mappingFunction) {
        this(Collections.singletonList(mappingFunction));
    }

    @SuppressWarnings("unchecked")
    public TriKeyFunction(List<TriMappingFunction<A, B, C>> mappingFunctionList) {
        this.mappingFunctions = mappingFunctionList.toArray(new TriMappingFunction[0]);
        this.mappingFunction0 = mappingFunctions.length > 0 ? mappingFunctions[0] : null;
        this.mappingFunction1 = mappingFunctions.length > 1 ? mappingFunctions[1] : null;
        this.mappingFunction2 = mappingFunctions.length > 2 ? mappingFunctions[2] : null;
        this.mappingFunction3 = mappingFunctions.length > 3 ? mappingFunctions[3] : null;
        this.path = switch (mappingFunctions.length) {
            case 1 -> this::apply1;
            case 2 -> this::apply2;
            case 3 -> this::apply3;
            case 4 -> this::apply4;
            default -> this::applyMany;
        };
    }

    @Override
    public Object apply(A a, B b, C c, Object oldKey) {
        return path.apply(a, b, c, oldKey);
    }

    private Object apply1(A a, B b, C c, Object oldKey) {
        return mappingFunction0.apply(a, b, c);
    }

    @SuppressWarnings("unchecked")
    private Object apply2(A a, B b, C c, Object oldKey) {
        var subkey1 = mappingFunction0.apply(a, b, c);
        var subkey2 = mappingFunction1.apply(a, b, c);
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
    private Object apply3(A a, B b, C c, Object oldKey) {
        var subkey1 = mappingFunction0.apply(a, b, c);
        var subkey2 = mappingFunction1.apply(a, b, c);
        var subkey3 = mappingFunction2.apply(a, b, c);
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
    private Object apply4(A a, B b, C c, Object oldKey) {
        var subkey1 = mappingFunction0.apply(a, b, c);
        var subkey2 = mappingFunction1.apply(a, b, c);
        var subkey3 = mappingFunction2.apply(a, b, c);
        var subkey4 = mappingFunction3.apply(a, b, c);
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

    private Object applyMany(A a, B b, C c, Object oldKey) {
        if (oldKey == null) {
            var result = new Object[mappingFunctions.length];
            for (var i = 0; i < mappingFunctions.length; i++) {
                result[i] = mappingFunctions[i].apply(a, b, c);
            }
            return new IndexerKey(result);
        }
        var oldArray = (Object[]) oldKey;
        var result = new Object[mappingFunctions.length];
        var subKeysEqual = true;
        for (var i = 0; i < mappingFunctions.length; i++) {
            var subkey = mappingFunctions[i].apply(a, b, c);
            subKeysEqual = subKeysEqual && Objects.equals(subkey, oldArray[i]);
            result[i] = subkey;
        }
        return new IndexerKey(result);
    }
}
