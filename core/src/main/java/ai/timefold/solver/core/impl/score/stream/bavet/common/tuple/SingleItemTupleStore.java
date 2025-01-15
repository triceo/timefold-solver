package ai.timefold.solver.core.impl.score.stream.bavet.common.tuple;

final class SingleItemTupleStore implements TupleStore {

    private Object value;

    @Override
    public <Value_> Value_ get(int index) {
        return (Value_) value;
    }

    @Override
    public void set(int index, Object value) {
        this.value = value;
    }

    @Override
    public <Value_> Value_ remove(int index) {
        Value_ oldValue = get(index);
        set(index, null);
        return oldValue;
    }

}
