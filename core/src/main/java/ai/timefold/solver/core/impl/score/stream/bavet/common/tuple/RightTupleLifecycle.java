package ai.timefold.solver.core.impl.score.stream.bavet.common.tuple;

public interface RightTupleLifecycle<Tuple_ extends Tuple> {

    void insertRight(Tuple_ tuple);

    void updateRight(Tuple_ tuple);

    void retractRight(Tuple_ tuple);

}
