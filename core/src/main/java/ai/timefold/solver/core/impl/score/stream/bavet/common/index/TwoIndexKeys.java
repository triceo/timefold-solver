package ai.timefold.solver.core.impl.score.stream.bavet.common.index;

import java.util.Objects;

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
        if (this == o) {
            return true;
        }
        return o instanceof TwoIndexKeys<?, ?> that &&
                Objects.equals(propertyA, that.propertyA) &&
                Objects.equals(propertyB, that.propertyB);
    }

    @Override
    public int hashCode() {
        var hash = 17;
        hash = 31 * hash + Objects.hashCode(propertyA);
        hash = 31 * hash + Objects.hashCode(propertyB);
        return hash;
    }
}
