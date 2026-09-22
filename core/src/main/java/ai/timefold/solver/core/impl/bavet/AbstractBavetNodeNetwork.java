package ai.timefold.solver.core.impl.bavet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Stream;

import ai.timefold.solver.core.impl.bavet.common.AbstractNode;
import ai.timefold.solver.core.impl.bavet.common.AbstractRootNode;
import ai.timefold.solver.core.impl.bavet.common.AbstractTwoInputNode;
import ai.timefold.solver.core.impl.bavet.common.DeferredSettleAware;
import ai.timefold.solver.core.impl.bavet.common.Propagator;
import ai.timefold.solver.core.impl.bavet.common.tuple.ActivitySupport;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Represents Bavet's network of nodes, specific to a particular session.
 */
@NullMarked
public abstract class AbstractBavetNodeNetwork {

    protected static AbstractNode[][] buildLayeredNodes(List<AbstractNode> nodeList) {
        var layerMap = new TreeMap<Long, List<AbstractNode>>();
        nodeList.forEach(node -> layerMap.computeIfAbsent(node.getLayerIndex(), unused -> new ArrayList<>()).add(node));
        var layerCount = layerMap.size();
        var layeredNodes = new AbstractNode[layerCount][];
        for (var i = 0; i < layerCount; i++) {
            var layer = layerMap.get((long) i);
            layeredNodes[i] = layer.toArray(new AbstractNode[0]);
        }
        return layeredNodes;
    }

    private final Map<Class<?>, List<AbstractRootNode<?>>> declaredClassToNodeMap;

    private final AbstractNode[][] layeredNodes;
    private final Function<AbstractNode, Propagator> propagatorFunction;
    /**
     * A subset of {@code layeredNodes}.
     * Once non-null, only contains propagators of nodes which are active.
     * See {@link ActivitySupport#isActive()} for details.
     */
    private Propagator @Nullable [][] layeredActivePropagators;
    /**
     * Aligned 1:1 with {@link #layeredActivePropagators} (same layer indices):
     * for each layer, the subset of its active nodes that implement {@link DeferredSettleAware}
     * and {@link DeferredSettleAware#canDeferWork()} returns {@code true};
     * i.e. filtering join and ifExists/ifNotExists nodes whose two inputs sit far enough apart.
     * Usually small or empty
     * (two-input nodes that can never defer never enqueue anything and are excluded here at build time)
     * so the common case pays nothing beyond an empty-array iteration in {@link #settleLayer}.
     */
    private DeferredSettleAware @Nullable [][] layeredActiveDeferredNodes;
    /**
     * Aligned 1:1 with {@link #layeredActivePropagators}:
     * for each layer, the indices of its propagators with pending work.
     * See {@link Propagator#setDirtyTracking}.
     */
    private BitSet @Nullable [] layeredDirtyBits;
    /**
     * Indices of layers in {@link #layeredActivePropagators} with at least one dirty propagator.
     */
    private final BitSet dirtyLayers = new BitSet();
    /**
     * Indices of layers in {@link #layeredActivePropagators} with at least one deferring node;
     * those must be visited on every settle, as their pending work only reaches the queue in prepareForSettle().
     */
    private BitSet deferredLayers = new BitSet();
    /**
     * For testing only:
     * the set of nodes that remained active after {@link #settle()};
     * null before settle.
     */
    private @Nullable Set<AbstractNode> activeNodeSet;

    /**
     * @param declaredClassToNodeMap starting nodes, one for each class used in the constraints;
     *        root nodes, layer index 0.
     * @param layeredNodes nodes grouped first by their layer, then by their index within the layer;
     *        propagation needs to happen in this order.
     */
    protected AbstractBavetNodeNetwork(Map<Class<?>, List<AbstractRootNode<?>>> declaredClassToNodeMap,
            AbstractNode[][] layeredNodes, Function<AbstractNode, Propagator> propagatorFunction) {
        this.declaredClassToNodeMap = declaredClassToNodeMap;
        this.layeredNodes = layeredNodes;
        this.propagatorFunction = propagatorFunction;
    }

    public int forEachNodeCount() {
        return declaredClassToNodeMap.size();
    }

    /**
     *
     * @param factClass
     * @return if {@link #isActivationCheckComplete()} is true, only returns active root nodes;
     *         otherwise returns all root nodes.
     *         This means that if this information was ever read before activation checks were complete,
     *         it should be re-read after to make sure no inactive nodes are included.
     */
    public Stream<AbstractRootNode<?>> getRootNodesAcceptingType(Class<?> factClass) {
        return declaredClassToNodeMap.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream())
                .filter(tupleSourceRoot -> tupleSourceRoot.allowsInstancesOf(factClass))
                .filter(node -> !isActivationCheckComplete() || activeNodeSet.contains(node));
    }

    public boolean isActivationCheckComplete() {
        return layeredActivePropagators != null;
    }

    public void settle() {
        // The very first settle fills an empty network.
        // A session does receive updates and retracts before it (setWorkingSolution updates shadow variables
        // between inserting the facts and settling), but no root tuple has reached TupleState.OK yet,
        // so each of those collapses inside the root node's own queue:
        // an update on a CREATING tuple is dropped, and a retract turns it ABORTING.
        // Every tuple that reaches a node past the roots therefore arrives exactly once, as an insert,
        // and none of them can be stale.
        // The deferring nodes go eager for the duration,
        // which spares them a second full cross-match walk in prepareForSettle().
        var preloadingActive = layeredActivePropagators == null;
        if (preloadingActive) {
            // Remove inactive nodes and settle the layers in one go.
            var initializedRootNodes = Collections.newSetFromMap(new IdentityHashMap<>());
            declaredClassToNodeMap.forEach((declaredClass, rootNodes) -> rootNodes.forEach(rootNode -> {
                if (initializedRootNodes.add(rootNode)) {
                    // Ensure one initialization per node.
                    // Root nodes are filled from a session, which can always produce.
                    rootNode.afterAllFactsInserted(true);
                }
            }));

            var activeNodes = Collections.<AbstractNode> newSetFromMap(new IdentityHashMap<>());
            var layeredActiveNodes = Arrays.stream(layeredNodes)
                    .map(layer -> Arrays.stream(layer)
                            .filter(s -> switch (s) {
                                case ActivitySupport activityEnabled -> activityEnabled.isActive();
                                case AbstractTwoInputNode<?, ?> twoInputNode -> twoInputNode.isActive();
                            })
                            .peek(activeNodes::add)
                            .toArray(AbstractNode[]::new))
                    .filter(layer -> layer.length > 0)
                    .toArray(AbstractNode[][]::new);
            this.activeNodeSet = activeNodes;
            layeredActivePropagators = Arrays.stream(layeredActiveNodes)
                    .map(layer -> Arrays.stream(layer).map(propagatorFunction).toArray(Propagator[]::new))
                    .toArray(Propagator[][]::new);
            layeredActiveDeferredNodes = Arrays.stream(layeredActiveNodes)
                    .map(layer -> Arrays.stream(layer)
                            .filter(s -> s instanceof DeferredSettleAware deferredSettleAware
                                    && deferredSettleAware.canDeferWork())
                            .map(DeferredSettleAware.class::cast)
                            .toArray(DeferredSettleAware[]::new))
                    .toArray(DeferredSettleAware[][]::new);
            initDirtyTracking(layeredActiveNodes);
            notifyPreload(true);
        }
        var i = nextLayer(0);
        while (i >= 0) {
            settleLayer(i);
            i = nextLayer(i + 1);
        }
        if (preloadingActive) {
            notifyPreload(false);
        }
    }

    private void initDirtyTracking(AbstractNode[][] layeredActiveNodes) {
        var layerCount = layeredActiveNodes.length;
        layeredDirtyBits = new BitSet[layerCount];
        deferredLayers = new BitSet(layerCount);
        for (var layer = 0; layer < layerCount; layer++) {
            var nodesInLayer = layeredActiveNodes[layer];
            var bits = new BitSet(nodesInLayer.length);
            for (var i = 0; i < nodesInLayer.length; i++) {
                // The raw queue, not the decorated propagator; that is what the node writes into.
                nodesInLayer[i].getPropagator().setDirtyTracking(dirtyLayers, layer, bits, i);
            }
            bits.set(0, nodesInLayer.length); // The preload settle processes everything.
            layeredDirtyBits[layer] = bits;
            dirtyLayers.set(layer);
            if (layeredActiveDeferredNodes[layer].length > 0) {
                deferredLayers.set(layer);
            }
        }
    }

    private int nextLayer(int fromIndex) {
        var nextDirty = dirtyLayers.nextSetBit(fromIndex);
        var nextDeferred = deferredLayers.nextSetBit(fromIndex);
        if (nextDirty < 0) {
            return nextDeferred;
        } else if (nextDeferred < 0) {
            return nextDirty;
        } else {
            return Math.min(nextDirty, nextDeferred);
        }
    }

    private void notifyPreload(boolean preloadingActive) {
        for (var layer : layeredActiveDeferredNodes) {
            for (var node : layer) {
                if (preloadingActive) {
                    node.preloadStarted();
                } else {
                    node.preloadEnded();
                }
            }
        }
    }

    private void settleLayer(int layerId) {
        for (var node : layeredActiveDeferredNodes[layerId]) {
            node.prepareForSettle();
        }
        var dirtyBits = layeredDirtyBits[layerId];
        var first = dirtyBits.nextSetBit(0);
        if (first < 0) { // Only visited for its deferred nodes, which had nothing to do.
            return;
        }
        var nodesInLayer = layeredActivePropagators[layerId];
        if (dirtyBits.nextSetBit(first + 1) < 0) { // Only one dirty node; avoid iteration.
            nodesInLayer[first].propagateEverything();
        } else {
            for (var i = first; i >= 0; i = dirtyBits.nextSetBit(i + 1)) {
                nodesInLayer[i].propagateRetracts();
            }
            for (var i = first; i >= 0; i = dirtyBits.nextSetBit(i + 1)) {
                nodesInLayer[i].propagateUpdates();
            }
            for (var i = first; i >= 0; i = dirtyBits.nextSetBit(i + 1)) {
                nodesInLayer[i].propagateInserts();
            }
        }
        dirtyBits.clear();
        dirtyLayers.clear(layerId);
    }

    Set<AbstractNode> getActiveNodes() {
        if (activeNodeSet == null) {
            throw new IllegalStateException("Impossible state: getActiveNodes() called before settle().");
        }
        return activeNodeSet;
    }

    /**
     * For testing only. All nodes in the network, regardless of activity.
     */
    List<AbstractNode> getNodes() {
        return Arrays.stream(layeredNodes).flatMap(Arrays::stream).toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AbstractBavetNodeNetwork that))
            return false;
        return Objects.equals(declaredClassToNodeMap, that.declaredClassToNodeMap)
                && Objects.deepEquals(layeredNodes, that.layeredNodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(declaredClassToNodeMap, Arrays.deepHashCode(layeredNodes));
    }

    @Override
    public String toString() {
        return "%s with %d forEach nodes.".formatted(getClass().getSimpleName(), forEachNodeCount());
    }

}
