package ai.timefold.solver.core.impl.score.stream.bavet.common.tuple;

public sealed interface BiTuple<A, B> extends UniTuple<A> permits TriTuple {

    B getB();

    void setB(B b);

}
