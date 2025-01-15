package ai.timefold.solver.core.impl.score.stream.bavet.common.tuple;

public sealed interface QuadTuple<A, B, C, D> extends TriTuple<A, B, C> permits UniversalTuple {

    D getD();

    void setD(D d);

}
