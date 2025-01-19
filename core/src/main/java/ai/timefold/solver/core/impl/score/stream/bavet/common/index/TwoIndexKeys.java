package ai.timefold.solver.core.impl.score.stream.bavet.common.index;

record TwoIndexKeys<A, B>(A keyA, B keyB) implements IndexKeys {

    @SuppressWarnings("unchecked")
    @Override
    public <Key_> Key_ get(int id) {
        return (Key_) switch (id) {
            case 0 -> keyA;
            case 1 -> keyB;
            default -> throw new IllegalArgumentException("Impossible state: index (%d) > 1"
                    .formatted(id));
        };
    }

}
