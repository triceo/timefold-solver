package ai.timefold.solver.core.impl.score.stream.bavet.quad;

import java.util.Objects;

import ai.timefold.solver.core.api.function.QuadFunction;
import ai.timefold.solver.core.impl.score.stream.bavet.common.AbstractMapNode;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.QuadTuple;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.TriTuple;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.TupleLifecycle;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.UniversalTuple;

final class MapQuadToTriNode<A, B, C, D, NewA, NewB, NewC>
        extends AbstractMapNode<QuadTuple<A, B, C, D>, TriTuple<NewA, NewB, NewC>> {

    private final QuadFunction<A, B, C, D, NewA> mappingFunctionA;
    private final QuadFunction<A, B, C, D, NewB> mappingFunctionB;
    private final QuadFunction<A, B, C, D, NewC> mappingFunctionC;

    MapQuadToTriNode(int mapStoreIndex, QuadFunction<A, B, C, D, NewA> mappingFunctionA,
            QuadFunction<A, B, C, D, NewB> mappingFunctionB, QuadFunction<A, B, C, D, NewC> mappingFunctionC,
            TupleLifecycle<TriTuple<NewA, NewB, NewC>> nextNodesTupleLifecycle, int outputStoreSize) {
        super(mapStoreIndex, nextNodesTupleLifecycle, outputStoreSize);
        this.mappingFunctionA = Objects.requireNonNull(mappingFunctionA);
        this.mappingFunctionB = Objects.requireNonNull(mappingFunctionB);
        this.mappingFunctionC = Objects.requireNonNull(mappingFunctionC);
    }

    @Override
    protected TriTuple<NewA, NewB, NewC> map(QuadTuple<A, B, C, D> tuple) {
        A factA = tuple.getA();
        B factB = tuple.getB();
        C factC = tuple.getC();
        D factD = tuple.getD();
        return new UniversalTuple<>(
                mappingFunctionA.apply(factA, factB, factC, factD),
                mappingFunctionB.apply(factA, factB, factC, factD),
                mappingFunctionC.apply(factA, factB, factC, factD),
                outputStoreSize);
    }

    @Override
    protected void remap(QuadTuple<A, B, C, D> inTuple, TriTuple<NewA, NewB, NewC> outTuple) {
        A factA = inTuple.getA();
        B factB = inTuple.getB();
        C factC = inTuple.getC();
        D factD = inTuple.getD();
        NewA newA = mappingFunctionA.apply(factA, factB, factC, factD);
        NewB newB = mappingFunctionB.apply(factA, factB, factC, factD);
        NewC newC = mappingFunctionC.apply(factA, factB, factC, factD);
        outTuple.setA(newA);
        outTuple.setB(newB);
        outTuple.setC(newC);
    }

}
