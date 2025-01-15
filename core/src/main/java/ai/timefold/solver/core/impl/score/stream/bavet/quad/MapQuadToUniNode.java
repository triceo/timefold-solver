package ai.timefold.solver.core.impl.score.stream.bavet.quad;

import java.util.Objects;

import ai.timefold.solver.core.api.function.QuadFunction;
import ai.timefold.solver.core.impl.score.stream.bavet.common.AbstractMapNode;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.QuadTuple;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.TupleLifecycle;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.UniTuple;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.UniversalTuple;

final class MapQuadToUniNode<A, B, C, D, NewA> extends AbstractMapNode<QuadTuple<A, B, C, D>, UniTuple<NewA>> {

    private final QuadFunction<A, B, C, D, NewA> mappingFunction;

    MapQuadToUniNode(int mapStoreIndex, QuadFunction<A, B, C, D, NewA> mappingFunction,
            TupleLifecycle<UniTuple<NewA>> nextNodesTupleLifecycle, int outputStoreSize) {
        super(mapStoreIndex, nextNodesTupleLifecycle, outputStoreSize);
        this.mappingFunction = Objects.requireNonNull(mappingFunction);
    }

    @Override
    protected UniTuple<NewA> map(QuadTuple<A, B, C, D> tuple) {
        A factA = tuple.getA();
        B factB = tuple.getB();
        C factC = tuple.getC();
        D factD = tuple.getD();
        return new UniversalTuple<>(mappingFunction.apply(factA, factB, factC, factD), outputStoreSize);
    }

    @Override
    protected void remap(QuadTuple<A, B, C, D> inTuple, UniTuple<NewA> outTuple) {
        A factA = inTuple.getA();
        B factB = inTuple.getB();
        C factC = inTuple.getC();
        D factD = inTuple.getD();
        NewA newA = mappingFunction.apply(factA, factB, factC, factD);
        outTuple.setA(newA);
    }

}
