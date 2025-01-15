package ai.timefold.solver.core.impl.score.stream.bavet.common.tuple;

final class ArrayBackedTupleStore implements TupleStore {

    private final Object[] values;

    public ArrayBackedTupleStore(int size) {
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
