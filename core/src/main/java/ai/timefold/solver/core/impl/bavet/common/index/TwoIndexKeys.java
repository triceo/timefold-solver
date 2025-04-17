package ai.timefold.solver.core.impl.bavet.common.index;

import java.util.Objects;

import ai.timefold.solver.core.impl.util.ObjectUtils;

record TwoIndexKeys<A, B>(A propertyA, B propertyB) implements IndexKeys {

    @SuppressWarnings("unchecked")
    @Override
    public <Key_> Key_ get(int id) {
        return (Key_) switch (id) {
            case 0 -> propertyA;
            case 1 -> propertyB;
            default -> throw new IllegalArgumentException("Impossible state: index (%d) > 1"
                    .formatted(id));
        };
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TwoIndexKeys<?, ?> other &&
                Objects.equals(propertyA, other.propertyA) &&
                Objects.equals(propertyB, other.propertyB);
    }

    @Override
    public int hashCode() {
        return ObjectUtils.hashCode(propertyA, propertyB);
    }

}
