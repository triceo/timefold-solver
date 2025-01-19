package ai.timefold.solver.core.impl.score.stream.bavet.common.index;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ai.timefold.solver.core.api.function.PentaFunction;
import ai.timefold.solver.core.impl.util.Pair;
import ai.timefold.solver.core.impl.util.Quadruple;
import ai.timefold.solver.core.impl.util.Triple;

final class QuadKeyFunction<A, B, C, D>
        implements PentaFunction<A, B, C, D, Object, Object>, KeyFunction {

    private final int mappingFunctionCount;
    private final QuadMappingFunction<A, B, C, D>[] mappingFunctions;
    private final QuadMappingFunction<A, B, C, D> mappingFunction0;
    private final QuadMappingFunction<A, B, C, D> mappingFunction1;
    private final QuadMappingFunction<A, B, C, D> mappingFunction2;
    private final QuadMappingFunction<A, B, C, D> mappingFunction3;
    private final PentaFunction<A, B, C, D, Object, Object> path;

    public QuadKeyFunction(QuadMappingFunction<A, B, C, D> mappingFunction) {
        this(Collections.singletonList(mappingFunction));
    }

    @SuppressWarnings("unchecked")
    public QuadKeyFunction(List<QuadMappingFunction<A, B, C, D>> mappingFunctionList) {
        this.mappingFunctionCount = mappingFunctionList.size();
        this.mappingFunctions = mappingFunctionList.toArray(new QuadMappingFunction[0]);
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
    public Object apply(A a, B b, C c, D d, Object oldKey) {
        return path.apply(a, b, c, d, oldKey);
    }

    private Object apply1(A a, B b, C c, D d, Object oldKey) {
        return mappingFunction0.apply(a, b, c, d);
    }

    @SuppressWarnings("unchecked")
    private Object apply2(A a, B b, C c, D d, Object oldKey) {
        var subkey1 = mappingFunction0.apply(a, b, c, d);
        var subkey2 = mappingFunction1.apply(a, b, c, d);
        if (oldKey == null) {
            return new Pair<>(subkey1, subkey2);
        }
        return ((Pair<Object, Object>) oldKey).newIfDifferent(subkey1, subkey2);
    }

    @SuppressWarnings("unchecked")
    private Object apply3(A a, B b, C c, D d, Object oldKey) {
        var subkey1 = mappingFunction0.apply(a, b, c, d);
        var subkey2 = mappingFunction1.apply(a, b, c, d);
        var subkey3 = mappingFunction2.apply(a, b, c, d);
        if (oldKey == null) {
            return new Triple<>(subkey1, subkey2, subkey3);
        }
        return ((Triple<Object, Object, Object>) oldKey).newIfDifferent(subkey1, subkey2, subkey3);
    }

    @SuppressWarnings("unchecked")
    private Object apply4(A a, B b, C c, D d, Object oldKey) {
        var subkey1 = mappingFunction0.apply(a, b, c, d);
        var subkey2 = mappingFunction1.apply(a, b, c, d);
        var subkey3 = mappingFunction2.apply(a, b, c, d);
        var subkey4 = mappingFunction3.apply(a, b, c, d);
        if (oldKey == null) {
            return new Quadruple<>(subkey1, subkey2, subkey3, subkey4);
        }
        return ((Quadruple<Object, Object, Object, Object>) oldKey).newIfDifferent(subkey1, subkey2, subkey3, subkey4);
    }

    private Object applyMany(A a, B b, C c, D d, Object oldKey) {
        var result = new Object[mappingFunctionCount];
        if (oldKey == null) {
            for (var i = 0; i < mappingFunctionCount; i++) {
                result[i] = mappingFunctions[i].apply(a, b, c, d);
            }
        } else {
            var oldArray = (Object[]) oldKey;
            var subKeysEqual = true;
            for (var i = 0; i < mappingFunctionCount; i++) {
                var subkey = mappingFunctions[i].apply(a, b, c, d);
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
