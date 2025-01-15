package ai.timefold.solver.core.impl.score.stream.bavet.bi;

import java.util.Objects;
import java.util.function.BiFunction;

import ai.timefold.solver.core.impl.score.stream.bavet.common.AbstractMapNode;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.BiTuple;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.TupleLifecycle;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.UniTuple;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.UniversalTuple;

final class MapBiToUniNode<A, B, NewA> extends AbstractMapNode<BiTuple<A, B>, UniTuple<NewA>> {

    private final BiFunction<A, B, NewA> mappingFunction;

    MapBiToUniNode(int mapStoreIndex, BiFunction<A, B, NewA> mappingFunction,
            TupleLifecycle<UniTuple<NewA>> nextNodesTupleLifecycle, int outputStoreSize) {
        super(mapStoreIndex, nextNodesTupleLifecycle, outputStoreSize);
        this.mappingFunction = Objects.requireNonNull(mappingFunction);
    }

    @Override
    protected UniTuple<NewA> map(BiTuple<A, B> tuple) {
        A factA = tuple.getA();
        B factB = tuple.getB();
        return new UniversalTuple<>(
                mappingFunction.apply(factA, factB),
                outputStoreSize);
    }

    @Override
    protected void remap(BiTuple<A, B> inTuple, UniTuple<NewA> outTuple) {
        A factA = inTuple.getA();
        B factB = inTuple.getB();
        NewA newA = mappingFunction.apply(factA, factB);
        outTuple.setA(newA);
    }

}
