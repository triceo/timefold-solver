package ai.timefold.solver.core.impl.score.stream.bavet.common.tuple;

final class ArrayBackedTupleStore implements TupleStore {

    private final Object[] values;

    public ArrayBackedTupleStore(int size) {
        values = new Object[size];
    }

    @Override
    public <Value_> Value_ get(int index) {
        return (Value_) values[index];
    }

    @Override
    public void set(int index, Object value) {
        this.values[index] = value;
    }

    @Override
    public <Value_> Value_ remove(int index) {
        var oldValue = get(index);
        set(index, null);
        return (Value_) oldValue;
    }

}
