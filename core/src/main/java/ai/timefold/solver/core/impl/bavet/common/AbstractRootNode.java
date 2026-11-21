package ai.timefold.solver.core.impl.bavet.common;

import ai.timefold.solver.core.impl.bavet.common.tuple.ActivitySupport;

import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract non-sealed class AbstractRootNode<A>
        extends AbstractNode
        implements ActivitySupport {

    public abstract boolean allowsInstancesOf(Class<?> clazz);

    public abstract Class<?>[] getSourceClasses();

}
