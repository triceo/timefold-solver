package ai.timefold.solver.core.impl.score.stream.bavet.uni;

import java.util.Objects;
import java.util.function.Function;

import ai.timefold.solver.core.impl.score.stream.bavet.common.AbstractMapNode;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.TupleLifecycle;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.UniTuple;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.UniversalTuple;

final class MapUniToUniNode<A, NewA> extends AbstractMapNode<UniTuple<A>, UniTuple<NewA>> {

    private final Function<A, NewA> mappingFunction;

    MapUniToUniNode(int mapStoreIndex, Function<A, NewA> mappingFunction,
            TupleLifecycle<UniTuple<NewA>> nextNodesTupleLifecycle, int outputStoreSize) {
        super(mapStoreIndex, nextNodesTupleLifecycle, outputStoreSize);
        this.mappingFunction = Objects.requireNonNull(mappingFunction);
    }

    @Override
    protected UniTuple<NewA> map(UniTuple<A> tuple) {
        A factA = tuple.getA();
        return new UniversalTuple<>(
                mappingFunction.apply(factA),
                outputStoreSize);
    }

    @Override
    protected void remap(UniTuple<A> inTuple, UniTuple<NewA> outTuple) {
        A factA = inTuple.getA();
        NewA newA = mappingFunction.apply(factA);
        outTuple.setA(newA);
    }

}
