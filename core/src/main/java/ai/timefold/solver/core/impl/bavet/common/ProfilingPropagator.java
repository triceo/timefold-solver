package ai.timefold.solver.core.impl.bavet.common;

import java.util.BitSet;

public record ProfilingPropagator(InnerConstraintProfiler profiler, ConstraintNodeProfileId profileId,
        Propagator delegate) implements Propagator {

    @Override
    public void propagateRetracts() {
        profiler.measure(profileId,
                InnerConstraintProfiler.Operation.RETRACT,
                delegate::propagateRetracts);
    }

    @Override
    public void propagateUpdates() {
        profiler.measure(profileId,
                InnerConstraintProfiler.Operation.UPDATE,
                delegate::propagateUpdates);
    }

    @Override
    public void setDirtyTracking(BitSet dirtyLayers, int layerIndex, BitSet layerDirtyBits, int index) {
        delegate.setDirtyTracking(dirtyLayers, layerIndex, layerDirtyBits, index);
    }

    @Override
    public void propagateInserts() {
        profiler.measure(profileId,
                InnerConstraintProfiler.Operation.INSERT,
                delegate::propagateInserts);
    }
}
