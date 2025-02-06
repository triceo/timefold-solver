package ai.timefold.solver.core.api.score.stream.tmp;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.bi.BiConstraintBuilder;
import ai.timefold.solver.core.api.score.stream.quad.QuadConstraintBuilder;
import ai.timefold.solver.core.api.score.stream.tri.TriConstraintBuilder;
import ai.timefold.solver.core.api.score.stream.uni.UniConstraintBuilder;

import org.jspecify.annotations.NullMarked;

/**
 * A {@link ConstraintProvider} that composes multiple {@link ConstraintDescriptor constraint descriptors}.
 * Extend to be able to mix constraints from multiple sources.
 * Use {@link #add(ConstraintDescriptor)} to add a constraint.
 * Use {@link #addAll(ComposableConstraintProvider)} to add all constraints from another {@link ComposableConstraintProvider},
 * on which {@link #acceptUpstream(ConstraintDescriptor)} method returns true.
 * Override {@link #acceptUpstream(ConstraintDescriptor)} to limit the constraints that are accepted from other
 * {@link ComposableConstraintProvider}s.
 * 
 * @param <Score_> the {@link Score} type
 */
@NullMarked
public abstract class ComposableConstraintProvider<Score_ extends Score<Score_>>
        implements ConstraintProvider {

    private final Set<ConstraintDescriptor<Score_>> constraintDescriptorSet = new LinkedHashSet<>();

    /**
     * Adds all constraints from another {@link ComposableConstraintProvider}.
     *
     * @param constraintProvider the constraint provider to add constraints from
     * @return true if the collection of constraints in this provider changed as a result of the call
     */
    public final boolean addAll(ComposableConstraintProvider<Score_> constraintProvider) {
        var size = constraintDescriptorSet.size();
        constraintProvider.getConstraintDescriptors()
                .filter(this::acceptUpstream)
                .forEach(this::add);
        return size != constraintDescriptorSet.size();
    }

    /**
     * Adds a constraint to this provider.
     * Constraints added through this method will not be affected by {@link #acceptUpstream(ConstraintDescriptor)}.
     *
     * @param constraintDescriptor the constraint descriptor to add
     * @return true if this provider did not already contain the specified constraint descriptor
     */
    public final boolean add(ConstraintDescriptor<Score_> constraintDescriptor) {
        return constraintDescriptorSet.add(constraintDescriptor);
    }

    private Stream<ConstraintDescriptor<Score_>> getConstraintDescriptors() {
        return constraintDescriptorSet.stream();
    }

    /**
     * Determines whether a constraint descriptor supplied via {@link #addAll(ComposableConstraintProvider)} should be added into this provider.
     * Unless overridden by a subclass, all constraint descriptors are accepted.
     *
     * @param constraintDescriptor the constraint descriptor to check
     * @return true if the constraint descriptor should be added into this provider
     */
    protected boolean acceptUpstream(ConstraintDescriptor<Score_> constraintDescriptor) {
        return true;
    }

    @Override
    public final Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return getConstraintDescriptors()
                .map(constraintDescriptor -> assemble(constraintDescriptor, constraintFactory))
                .toArray(Constraint[]::new);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static <Score_ extends Score<Score_>> Constraint assemble(ConstraintDescriptor<Score_> constraintDescriptor,
            ConstraintFactory constraintFactory) {
        var builder = constraintDescriptor.buildConstraint(constraintFactory)
                .usingDefaultConstraintWeight(constraintDescriptor.defaultConstraintWeight());
        if (builder instanceof UniConstraintBuilder uniBuilder) {
            var uniConstraintDescriptor = (UniConstraintDescriptor) constraintDescriptor;
            if (uniConstraintDescriptor.justificationFunction() != null) {
                uniBuilder = uniBuilder.justifyWith(uniConstraintDescriptor.justificationFunction());
            }
            if (constraintDescriptor.indictmentFunction() != null) {
                uniBuilder = uniBuilder.indictWith(uniConstraintDescriptor.indictmentFunction());
            }
            return uniBuilder.asConstraintDescribed(constraintDescriptor.name(),
                    constraintDescriptor.description(), constraintDescriptor.group());
        } else if (builder instanceof BiConstraintBuilder biBuilder) {
            var biConstraintDescriptor = (BiConstraintDescriptor) constraintDescriptor;
            if (constraintDescriptor.justificationFunction() != null) {
                biBuilder = biBuilder.justifyWith(biConstraintDescriptor.justificationFunction());
            }
            if (constraintDescriptor.indictmentFunction() != null) {
                biBuilder = biBuilder.indictWith(biConstraintDescriptor.indictmentFunction());
            }
            return biBuilder.asConstraintDescribed(constraintDescriptor.name(),
                    constraintDescriptor.description(), constraintDescriptor.group());
        } else if (builder instanceof TriConstraintBuilder triBuilder) {
            var triConstraintDescriptor = (TriConstraintDescriptor) constraintDescriptor;
            if (triConstraintDescriptor.justificationFunction() != null) {
                triBuilder = triBuilder.justifyWith(triConstraintDescriptor.justificationFunction());
            }
            if (constraintDescriptor.indictmentFunction() != null) {
                triBuilder = triBuilder.indictWith(triConstraintDescriptor.indictmentFunction());
            }
            return triBuilder.asConstraintDescribed(constraintDescriptor.name(),
                    constraintDescriptor.description(), constraintDescriptor.group());
        } else if (builder instanceof QuadConstraintBuilder quadBuilder) {
            var quadConstraintDescriptor = (QuadConstraintDescriptor) constraintDescriptor;
            if (quadConstraintDescriptor.justificationFunction() != null) {
                quadBuilder = quadBuilder.justifyWith(quadConstraintDescriptor.justificationFunction());
            }
            if (constraintDescriptor.indictmentFunction() != null) {
                quadBuilder = quadBuilder.indictWith(quadConstraintDescriptor.indictmentFunction());
            }
            return quadBuilder.asConstraintDescribed(constraintDescriptor.name(),
                    constraintDescriptor.description(), constraintDescriptor.group());
        } else {
            throw new UnsupportedOperationException("Impossible state: Unsupported constraint cardinality: " + builder);
        }
    }

}
