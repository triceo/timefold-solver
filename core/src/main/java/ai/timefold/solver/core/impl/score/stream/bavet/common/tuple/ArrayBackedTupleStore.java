package ai.timefold.solver.core.impl.score.stream.bavet.common.tuple;

final class ArrayBackedTupleStore implements TupleStore {

    private static final ArrayBackedTupleStore EMPTY = new ArrayBackedTupleStore(0);

    static TupleStore ofSize(int size) {
        return size == 0 ? EMPTY : new ArrayBackedTupleStore(size);
    }

    private final Object[] values;

    private ArrayBackedTupleStore(int size) {
        values = new Object[size];
    }

    @Override
    public Object get(int index) {
        return values[index];
    }

    @Override
    public void set(int index, Object value) {
        values[index] = value;
    }

}
