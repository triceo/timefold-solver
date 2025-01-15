package ai.timefold.solver.core.impl.score.stream.bavet.common.tuple;

final class SingleItemTupleStore implements TupleStore {

    private Object value;

    @Override
    public Object get(int index) {
        return this.value;
    }

    @Override
    public void set(int index, Object value) {
        this.value = value;
    }

}
