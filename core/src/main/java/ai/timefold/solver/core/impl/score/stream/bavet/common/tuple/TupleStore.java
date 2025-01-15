package ai.timefold.solver.core.impl.score.stream.bavet.common.tuple;

sealed interface TupleStore permits ArrayBackedTupleStore, SingleItemTupleStore {

    static TupleStore ofSize(int size) {
        return switch (size) {
            case 0 -> ArrayBackedTupleStore.EMPTY;
            case 1 -> new SingleItemTupleStore();
            default -> new ArrayBackedTupleStore(size);
        };
    }

    Object get(int index);

    void set(int index, Object value);

    default Object remove(int index) {
        var oldValue = get(index);
        set(index, null);
        return oldValue;
    }

}
