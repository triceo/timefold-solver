package ai.timefold.solver.core.impl.score.stream.bavet.common.tuple;

sealed interface TupleStore permits ArrayBackedTupleStore, SingleItemTupleStore {

    static TupleStore ofSize(int size) {
        if (size == 1) {
            return new SingleItemTupleStore();
        } else {
            return ArrayBackedTupleStore.ofSize(size);
        }
    }

    Object get(int index);

    void set(int index, Object value);

    default Object remove(int index) {
        var oldValue = get(index);
        set(index, null);
        return oldValue;
    }

}
