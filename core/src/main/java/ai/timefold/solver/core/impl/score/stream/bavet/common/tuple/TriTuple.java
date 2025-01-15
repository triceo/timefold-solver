package ai.timefold.solver.core.impl.score.stream.bavet.common.tuple;

public sealed interface TriTuple<A, B, C> extends BiTuple<A, B> permits QuadTuple {

    C getC();

    void setC(C c);

}
