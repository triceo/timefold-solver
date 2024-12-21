package ai.timefold.solver.core.impl.score.stream.bavet.common;

import java.util.List;
import java.util.Objects;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.ConstraintStream;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.AbstractTuple;
import ai.timefold.solver.core.impl.score.stream.bavet.common.tuple.TupleLifecycle;
import ai.timefold.solver.core.impl.util.Pair;
import ai.timefold.solver.core.impl.util.Quadruple;
import ai.timefold.solver.core.impl.util.Triple;

/**
 * Each Group...Node with at least one collector has a constructor with the following signature:
 * {@code Group...Node(<keyMappings>, int groupStoreIndex, <collectors>, TupleLifecycle<Tuple_> nextNodesTupleLifecycle,
 * int outputStoreSize, Environment environmentMode)}.
 * <p>
 * The Group...Nodes with no collectors have a constructor with the following signature:
 * {@code Group...Node(<keyMappings>, int groupStoreIndex, TupleLifecycle<Tuple_> nextNodesTupleLifecycle, 
 * int outputStoreSize, Environment environmentMode)}.
 * <p>
 * The interfaces in this file correspond to each of the possible signatures of the Group...Node constructor.
 * These interfaces are thus covariant with a particular GroupXMappingYCollector...Node signature,
 * allowing a method reference to be used.
 * To reduce the number of interfaces, we use Collector..._ and Key..._ generics
 * (instead of the classes UniConstraintCollector/Function, BiConstraintCollector/BiFunction, ...).
 */
public record GroupNodeConstructor<Tuple_ extends AbstractTuple>(Object equalityKey,
        NodeConstructor<Tuple_> nodeConstructorFunction) {

    public static <CollectorA_, Tuple_ extends AbstractTuple> GroupNodeConstructor<Tuple_>
            zeroKeysGroupBy(CollectorA_ collector, GroupBy0Mapping1CollectorNodeBuilder<CollectorA_, Tuple_> builder) {
        return new GroupNodeConstructor<>(collector,
                (groupStoreIndex, nextNodesTupleLifecycle, outputStoreSize, environmentMode) -> builder.build(groupStoreIndex,
                        collector, nextNodesTupleLifecycle, outputStoreSize, environmentMode));
    }

    public static <CollectorA_, CollectorB_, Tuple_ extends AbstractTuple> GroupNodeConstructor<Tuple_>
            zeroKeysGroupBy(CollectorA_ collectorA, CollectorB_ collectorB,
                    GroupBy0Mapping2CollectorNodeBuilder<CollectorA_, CollectorB_, Tuple_> builder) {
        return new GroupNodeConstructor<>(new Pair<>(collectorA, collectorB),
                (groupStoreIndex, nextNodesTupleLifecycle, outputStoreSize, environmentMode) -> builder.build(groupStoreIndex,
                        collectorA, collectorB, nextNodesTupleLifecycle, outputStoreSize, environmentMode));
    }

    public static <CollectorA_, CollectorB_, CollectorC_, Tuple_ extends AbstractTuple> GroupNodeConstructor<Tuple_>
            zeroKeysGroupBy(CollectorA_ collectorA, CollectorB_ collectorB, CollectorC_ collectorC,
                    GroupBy0Mapping3CollectorNodeBuilder<CollectorA_, CollectorB_, CollectorC_, Tuple_> builder) {
        return new GroupNodeConstructor<>(new Triple<>(collectorA, collectorB, collectorC),
                (groupStoreIndex, nextNodesTupleLifecycle, outputStoreSize, environmentMode) -> builder.build(groupStoreIndex,
                        collectorA, collectorB, collectorC, nextNodesTupleLifecycle, outputStoreSize, environmentMode));
    }

    public static <CollectorA_, CollectorB_, CollectorC_, CollectorD_, Tuple_ extends AbstractTuple>
            GroupNodeConstructor<Tuple_>
            zeroKeysGroupBy(CollectorA_ collectorA, CollectorB_ collectorB, CollectorC_ collectorC, CollectorD_ collectorD,
                    GroupBy0Mapping4CollectorNodeBuilder<CollectorA_, CollectorB_, CollectorC_, CollectorD_, Tuple_> builder) {
        return new GroupNodeConstructor<>(new Quadruple<>(collectorA, collectorB, collectorC, collectorD),
                (groupStoreIndex, nextNodesTupleLifecycle, outputStoreSize, environmentMode) -> builder.build(groupStoreIndex,
                        collectorA, collectorB, collectorC, collectorD, nextNodesTupleLifecycle, outputStoreSize,
                        environmentMode));
    }

    public static <KeyA_, Tuple_ extends AbstractTuple> GroupNodeConstructor<Tuple_>
            oneKeyGroupBy(KeyA_ keyMapping, GroupBy1Mapping0CollectorNodeBuilder<KeyA_, Tuple_> builder) {
        return new GroupNodeConstructor<>(keyMapping,
                (groupStoreIndex, nextNodesTupleLifecycle, outputStoreSize, environmentMode) -> builder.build(keyMapping,
                        groupStoreIndex, nextNodesTupleLifecycle, outputStoreSize, environmentMode));
    }

    public static <KeyA_, CollectorB_, Tuple_ extends AbstractTuple> GroupNodeConstructor<Tuple_>
            oneKeyGroupBy(KeyA_ keyMappingA, CollectorB_ collectorB,
                    GroupBy1Mapping1CollectorNodeBuilder<KeyA_, CollectorB_, Tuple_> builder) {
        return new GroupNodeConstructor<>(new Pair<>(keyMappingA, collectorB),
                (groupStoreIndex, nextNodesTupleLifecycle, outputStoreSize, environmentMode) -> builder.build(keyMappingA,
                        groupStoreIndex, collectorB, nextNodesTupleLifecycle, outputStoreSize, environmentMode));
    }

    public static <KeyA_, CollectorB_, CollectorC_, Tuple_ extends AbstractTuple> GroupNodeConstructor<Tuple_>
            oneKeyGroupBy(KeyA_ keyMappingA, CollectorB_ collectorB, CollectorC_ collectorC,
                    GroupBy1Mapping2CollectorNodeBuilder<KeyA_, CollectorB_, CollectorC_, Tuple_> builder) {
        return new GroupNodeConstructor<>(new Triple<>(keyMappingA, collectorB, collectorC),
                (groupStoreIndex, nextNodesTupleLifecycle, outputStoreSize, environmentMode) -> builder.build(keyMappingA,
                        groupStoreIndex, collectorB, collectorC, nextNodesTupleLifecycle, outputStoreSize, environmentMode));
    }

    public static <KeyA_, CollectorB_, CollectorC_, CollectorD_, Tuple_ extends AbstractTuple> GroupNodeConstructor<Tuple_>
            oneKeyGroupBy(KeyA_ keyMappingA, CollectorB_ collectorB, CollectorC_ collectorC, CollectorD_ collectorD,
                    GroupBy1Mapping3CollectorNodeBuilder<KeyA_, CollectorB_, CollectorC_, CollectorD_, Tuple_> builder) {
        return new GroupNodeConstructor<>(new Quadruple<>(keyMappingA, collectorB, collectorC, collectorD),
                (groupStoreIndex, nextNodesTupleLifecycle, outputStoreSize, environmentMode) -> builder.build(keyMappingA,
                        groupStoreIndex, collectorB, collectorC, collectorD, nextNodesTupleLifecycle, outputStoreSize,
                        environmentMode));
    }

    public static <KeyA_, KeyB_, Tuple_ extends AbstractTuple> GroupNodeConstructor<Tuple_>
            twoKeysGroupBy(KeyA_ keyMappingA, KeyB_ keyMappingB,
                    GroupBy2Mapping0CollectorNodeBuilder<KeyA_, KeyB_, Tuple_> builder) {
        return new GroupNodeConstructor<>(new Pair<>(keyMappingA, keyMappingB),
                (groupStoreIndex, nextNodesTupleLifecycle, outputStoreSize, environmentMode) -> builder.build(keyMappingA,
                        keyMappingB, groupStoreIndex, nextNodesTupleLifecycle, outputStoreSize, environmentMode));
    }

    public static <KeyA_, KeyB_, CollectorC_, Tuple_ extends AbstractTuple> GroupNodeConstructor<Tuple_>
            twoKeysGroupBy(KeyA_ keyMappingA, KeyB_ keyMappingB, CollectorC_ collectorC,
                    GroupBy2Mapping1CollectorNodeBuilder<KeyA_, KeyB_, CollectorC_, Tuple_> builder) {
        return new GroupNodeConstructor<>(new Triple<>(keyMappingA, keyMappingB, collectorC),
                (groupStoreIndex, nextNodesTupleLifecycle, outputStoreSize, environmentMode) -> builder.build(keyMappingA,
                        keyMappingB, groupStoreIndex, collectorC, nextNodesTupleLifecycle, outputStoreSize, environmentMode));
    }

    public static <KeyA_, KeyB_, CollectorC_, CollectorD_, Tuple_ extends AbstractTuple> GroupNodeConstructor<Tuple_>
            twoKeysGroupBy(KeyA_ keyMappingA, KeyB_ keyMappingB, CollectorC_ collectorC, CollectorD_ collectorD,
                    GroupBy2Mapping2CollectorNodeBuilder<KeyA_, KeyB_, CollectorC_, CollectorD_, Tuple_> builder) {
        return new GroupNodeConstructor<>(new Quadruple<>(keyMappingA, keyMappingB, collectorC, collectorD),
                (groupStoreIndex, nextNodesTupleLifecycle, outputStoreSize, environmentMode) -> builder.build(keyMappingA,
                        keyMappingB, groupStoreIndex, collectorC, collectorD, nextNodesTupleLifecycle, outputStoreSize,
                        environmentMode));
    }

    public static <KeyA_, KeyB_, KeyC_, Tuple_ extends AbstractTuple> GroupNodeConstructor<Tuple_>
            threeKeysGroupBy(KeyA_ keyMappingA, KeyB_ keyMappingB, KeyC_ keyMappingC,
                    GroupBy3Mapping0CollectorNodeBuilder<KeyA_, KeyB_, KeyC_, Tuple_> builder) {
        return new GroupNodeConstructor<>(new Triple<>(keyMappingA, keyMappingB, keyMappingC),
                (groupStoreIndex, nextNodesTupleLifecycle, outputStoreSize, environmentMode) -> builder.build(keyMappingA,
                        keyMappingB, keyMappingC, groupStoreIndex, nextNodesTupleLifecycle, outputStoreSize, environmentMode));
    }

    public static <KeyA_, KeyB_, KeyC_, CollectorD_, Tuple_ extends AbstractTuple> GroupNodeConstructor<Tuple_>
            threeKeysGroupBy(KeyA_ keyMappingA, KeyB_ keyMappingB, KeyC_ keyMappingC, CollectorD_ collectorD,
                    GroupBy3Mapping1CollectorNodeBuilder<KeyA_, KeyB_, KeyC_, CollectorD_, Tuple_> builder) {
        return new GroupNodeConstructor<>(new Quadruple<>(keyMappingA, keyMappingB, keyMappingC, collectorD),
                (groupStoreIndex, nextNodesTupleLifecycle, outputStoreSize, environmentMode) -> builder.build(keyMappingA,
                        keyMappingB, keyMappingC, groupStoreIndex, collectorD, nextNodesTupleLifecycle, outputStoreSize,
                        environmentMode));
    }

    public static <KeyA_, KeyB_, KeyC_, KeyD_, Tuple_ extends AbstractTuple> GroupNodeConstructor<Tuple_>
            fourKeysGroupBy(KeyA_ keyMappingA, KeyB_ keyMappingB, KeyC_ keyMappingC, KeyD_ keyMappingD,
                    GroupBy4Mapping0CollectorNodeBuilder<KeyA_, KeyB_, KeyC_, KeyD_, Tuple_> builder) {
        return new GroupNodeConstructor<>(new Quadruple<>(keyMappingA, keyMappingB, keyMappingC, keyMappingD),
                (groupStoreIndex, nextNodesTupleLifecycle, outputStoreSize, environmentMode) -> builder.build(keyMappingA,
                        keyMappingB, keyMappingC, keyMappingD, groupStoreIndex, nextNodesTupleLifecycle, outputStoreSize,
                        environmentMode));
    }

    @FunctionalInterface
    public interface NodeConstructor<Tuple_ extends AbstractTuple> {

        AbstractNode apply(int groupStoreIndex, TupleLifecycle<Tuple_> nextNodesTupleLifecycle, int outputStoreSize,
                EnvironmentMode environmentMode);

    }

    @FunctionalInterface
    public interface GroupBy0Mapping1CollectorNodeBuilder<CollectorA_, Tuple_ extends AbstractTuple> {
        AbstractNode build(int groupStoreIndex, CollectorA_ collector, TupleLifecycle<Tuple_> nextNodesTupleLifecycle,
                int outputStoreSize, EnvironmentMode environmentMode);
    }

    @FunctionalInterface
    public interface GroupBy0Mapping2CollectorNodeBuilder<CollectorA_, CollectorB_, Tuple_ extends AbstractTuple> {
        AbstractNode build(int groupStoreIndex, CollectorA_ collectorA, CollectorB_ collectorB,
                TupleLifecycle<Tuple_> nextNodesTupleLifecycle, int outputStoreSize, EnvironmentMode environmentMode);
    }

    @FunctionalInterface
    public interface GroupBy0Mapping3CollectorNodeBuilder<CollectorA_, CollectorB_, CollectorC_, Tuple_ extends AbstractTuple> {
        AbstractNode build(int groupStoreIndex, CollectorA_ collectorA, CollectorB_ collectorB, CollectorC_ collectorC,
                TupleLifecycle<Tuple_> nextNodesTupleLifecycle, int outputStoreSize, EnvironmentMode environmentMode);
    }

    @FunctionalInterface
    public interface GroupBy0Mapping4CollectorNodeBuilder<CollectorA_, CollectorB_, CollectorC_, CollectorD_, Tuple_ extends AbstractTuple> {
        AbstractNode build(int groupStoreIndex, CollectorA_ collectorA, CollectorB_ collectorB, CollectorC_ collectorC,
                CollectorD_ collectorD, TupleLifecycle<Tuple_> nextNodesTupleLifecycle, int outputStoreSize,
                EnvironmentMode environmentMode);
    }

    @FunctionalInterface
    public interface GroupBy1Mapping0CollectorNodeBuilder<KeyA_, Tuple_ extends AbstractTuple> {
        AbstractNode build(KeyA_ keyMapping, int groupStoreIndex, TupleLifecycle<Tuple_> nextNodesTupleLifecycle,
                int outputStoreSize, EnvironmentMode environmentMode);
    }

    @FunctionalInterface
    public interface GroupBy2Mapping0CollectorNodeBuilder<KeyA_, KeyB_, Tuple_ extends AbstractTuple> {
        AbstractNode build(KeyA_ keyMappingA, KeyB_ keyMappingB, int groupStoreIndex,
                TupleLifecycle<Tuple_> nextNodesTupleLifecycle, int outputStoreSize, EnvironmentMode environmentMode);
    }

    @FunctionalInterface
    public interface GroupBy3Mapping0CollectorNodeBuilder<KeyA_, KeyB_, KeyC_, Tuple_ extends AbstractTuple> {
        AbstractNode build(KeyA_ keyMappingA, KeyB_ keyMappingB, KeyC_ keyMappingC, int groupStoreIndex,
                TupleLifecycle<Tuple_> nextNodesTupleLifecycle, int outputStoreSize, EnvironmentMode environmentMode);
    }

    @FunctionalInterface
    public interface GroupBy4Mapping0CollectorNodeBuilder<KeyA_, KeyB_, KeyC_, KeyD_, Tuple_ extends AbstractTuple> {
        AbstractNode build(KeyA_ keyMappingA, KeyB_ keyMappingB, KeyC_ keyMappingC, KeyD_ keyMappingD, int groupStoreIndex,
                TupleLifecycle<Tuple_> nextNodesTupleLifecycle, int outputStoreSize, EnvironmentMode environmentMode);
    }

    @FunctionalInterface
    public interface GroupBy1Mapping1CollectorNodeBuilder<KeyA_, CollectorB_, Tuple_ extends AbstractTuple> {
        AbstractNode build(KeyA_ keyMapping, int groupStoreIndex, CollectorB_ collector,
                TupleLifecycle<Tuple_> nextNodesTupleLifecycle, int outputStoreSize, EnvironmentMode environmentMode);
    }

    @FunctionalInterface
    public interface GroupBy1Mapping2CollectorNodeBuilder<KeyA_, CollectorB_, CollectorC_, Tuple_ extends AbstractTuple> {
        AbstractNode build(KeyA_ keyMapping, int groupStoreIndex, CollectorB_ collectorA, CollectorC_ collectorB,
                TupleLifecycle<Tuple_> nextNodesTupleLifecycle, int outputStoreSize, EnvironmentMode environmentMode);
    }

    @FunctionalInterface
    public interface GroupBy1Mapping3CollectorNodeBuilder<KeyA_, CollectorB_, CollectorC_, CollectorD_, Tuple_ extends AbstractTuple> {
        AbstractNode build(KeyA_ keyMapping, int groupStoreIndex, CollectorB_ collectorA, CollectorC_ collectorB,
                CollectorD_ collectorC, TupleLifecycle<Tuple_> nextNodesTupleLifecycle, int outputStoreSize,
                EnvironmentMode environmentMode);
    }

    @FunctionalInterface
    public interface GroupBy2Mapping1CollectorNodeBuilder<KeyA_, KeyB_, CollectorC_, Tuple_ extends AbstractTuple> {
        AbstractNode build(KeyA_ keyMappingA, KeyB_ keyMappingB, int groupStoreIndex, CollectorC_ collectorC,
                TupleLifecycle<Tuple_> nextNodesTupleLifecycle, int outputStoreSize, EnvironmentMode environmentMode);
    }

    @FunctionalInterface
    public interface GroupBy2Mapping2CollectorNodeBuilder<KeyA_, KeyB_, CollectorC_, CollectorD_, Tuple_ extends AbstractTuple> {
        AbstractNode build(KeyA_ keyMappingA, KeyB_ keyMappingB, int groupStoreIndex, CollectorC_ collectorC,
                CollectorD_ collectorD, TupleLifecycle<Tuple_> nextNodesTupleLifecycle, int outputStoreSize,
                EnvironmentMode environmentMode);
    }

    @FunctionalInterface
    public interface GroupBy3Mapping1CollectorNodeBuilder<KeyA_, KeyB_, KeyC_, CollectorD_, Tuple_ extends AbstractTuple> {
        AbstractNode build(KeyA_ keyMappingA, KeyB_ keyMappingB, KeyC_ keyMappingC, int groupStoreIndex, CollectorD_ collectorC,
                TupleLifecycle<Tuple_> nextNodesTupleLifecycle, int outputStoreSize, EnvironmentMode environmentMode);
    }

    public <Solution_, Score_ extends Score<Score_>> void build(NodeBuildHelper<Score_> buildHelper,
            BavetAbstractConstraintStream<Solution_> parentTupleSource,
            BavetAbstractConstraintStream<Solution_> aftStream, List<? extends ConstraintStream> aftStreamChildList,
            BavetAbstractConstraintStream<Solution_> bridgeStream, List<? extends ConstraintStream> bridgeStreamChildList,
            EnvironmentMode environmentMode) {
        if (!bridgeStreamChildList.isEmpty()) {
            throw new IllegalStateException("Impossible state: the stream (" + bridgeStream
                    + ") has an non-empty childStreamList (" + bridgeStreamChildList + ") but it's a groupBy bridge.");
        }
        var groupStoreIndex = buildHelper.reserveTupleStoreIndex(parentTupleSource);
        TupleLifecycle<Tuple_> tupleLifecycle = buildHelper.getAggregatedTupleLifecycle(aftStreamChildList);
        var outputStoreSize = buildHelper.extractTupleStoreSize(aftStream);
        var node = nodeConstructorFunction.apply(groupStoreIndex, tupleLifecycle, outputStoreSize, environmentMode);
        buildHelper.addNode(node, bridgeStream);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GroupNodeConstructor<?> that)) {
            return false;
        }
        return Objects.equals(equalityKey, that.equalityKey);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(equalityKey);
    }

}
