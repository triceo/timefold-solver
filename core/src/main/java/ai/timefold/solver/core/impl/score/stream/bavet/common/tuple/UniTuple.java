package ai.timefold.solver.core.impl.score.stream.bavet.common.tuple;

public sealed interface UniTuple<A> extends Tuple permits BiTuple {

    A getA();

    void setA(A a);

}
