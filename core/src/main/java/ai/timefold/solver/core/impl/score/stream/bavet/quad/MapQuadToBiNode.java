package ai.timefold.solver.core.impl.score.stream.bavet.quad;

import java.util.Objects;

import ai.timefold.solver.core.api.function.QuadFunction;
import ai.timefold.solver.core.impl.score.stream.bavet.common.AbstractMapNode;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.BiTuple;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.QuadTuple;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.TupleLifecycle;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.UniversalTuple;

final class MapQuadToBiNode<A, B, C, D, NewA, NewB> extends AbstractMapNode<QuadTuple<A, B, C, D>, BiTuple<NewA, NewB>> {

    private final QuadFunction<A, B, C, D, NewA> mappingFunctionA;
    private final QuadFunction<A, B, C, D, NewB> mappingFunctionB;

    MapQuadToBiNode(int mapStoreIndex, QuadFunction<A, B, C, D, NewA> mappingFunctionA,
            QuadFunction<A, B, C, D, NewB> mappingFunctionB, TupleLifecycle<BiTuple<NewA, NewB>> nextNodesTupleLifecycle,
            int outputStoreSize) {
        super(mapStoreIndex, nextNodesTupleLifecycle, outputStoreSize);
        this.mappingFunctionA = Objects.requireNonNull(mappingFunctionA);
        this.mappingFunctionB = Objects.requireNonNull(mappingFunctionB);
    }

    @Override
    protected BiTuple<NewA, NewB> map(QuadTuple<A, B, C, D> tuple) {
        A factA = tuple.getA();
        B factB = tuple.getB();
        C factC = tuple.getC();
        D factD = tuple.getD();
        return new UniversalTuple<>(
                mappingFunctionA.apply(factA, factB, factC, factD),
                mappingFunctionB.apply(factA, factB, factC, factD),
                outputStoreSize);
    }

    @Override
    protected void remap(QuadTuple<A, B, C, D> inTuple, BiTuple<NewA, NewB> outTuple) {
        A factA = inTuple.getA();
        B factB = inTuple.getB();
        C factC = inTuple.getC();
        D factD = inTuple.getD();
        NewA newA = mappingFunctionA.apply(factA, factB, factC, factD);
        NewB newB = mappingFunctionB.apply(factA, factB, factC, factD);
        outTuple.setA(newA);
        outTuple.setB(newB);
    }

}
