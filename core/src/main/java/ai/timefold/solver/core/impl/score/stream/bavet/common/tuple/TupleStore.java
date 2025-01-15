package ai.timefold.solver.core.impl.score.stream.bavet.common.tuple;

sealed interface TupleStore permits ArrayBackedTupleStore, SingleItemTupleStore {

    <Value_> Value_ get(int index);

    void set(int index, Object value);

    <Value_> Value_ remove(int index);

}
