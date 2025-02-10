package ai.timefold.solver.core.api.score.stream;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.bi.BiConstraintBuilder;
import ai.timefold.solver.core.api.score.stream.bi.BiConstraintDefinition;
import ai.timefold.solver.core.api.score.stream.quad.QuadConstraintBuilder;
import ai.timefold.solver.core.api.score.stream.quad.QuadConstraintDefinition;
import ai.timefold.solver.core.api.score.stream.tri.TriConstraintBuilder;
import ai.timefold.solver.core.api.score.stream.tri.TriConstraintDefinition;
import ai.timefold.solver.core.api.score.stream.uni.UniConstraintBuilder;
import ai.timefold.solver.core.api.score.stream.uni.UniConstraintDefinition;

import org.jspecify.annotations.NullMarked;

/**
 * A {@link ConstraintProvider} that composes multiple {@link ConstraintDefinition constraint descriptors}.
 * Extend to be able to mix constraints from multiple sources.
 * <p>
 * Use {@link #add(ConstraintDefinition)} to add a single constraint,
 * or {@link #addAll(ComposableConstraintProvider)} to add all constraints from another {@link ComposableConstraintProvider},
 * <p>
 * Override {@link #filter(ConstraintDefinition)} to limit the constraints that are accepted.
 * 
 * @param <Score_> the {@link Score} type
 */
@NullMarked
public abstract class ComposableConstraintProvider<Score_ extends Score<Score_>>
        implements ConstraintProvider {

    private final Set<ConstraintDefinition<Score_>> constraintDefinitionSet = new LinkedHashSet<>();

    /**
     * Adds all constraints from another {@link ComposableConstraintProvider}.
     * Constraints will only be added if they pass {@link #filter(ConstraintDefinition) the filter}.
     *
     * @param constraintProvider the constraint provider to add constraints from
     * @return true if the collection of constraints in this provider changed as a result of the call
     */
    protected final boolean addAll(ComposableConstraintProvider<Score_> constraintProvider) {
        var size = constraintDefinitionSet.size();
        constraintProvider.getConstraintDescriptors()
                .filter(this::filter)
                .forEach(this::add);
        return size != constraintDefinitionSet.size();
    }

    /**
     * Adds a constraint to this provider.
     * Constraints will only be added if they pass {@link #filter(ConstraintDefinition) the filter}.
     *
     * @param constraintDefinition the constraint descriptor to add
     * @return true if the collection of constraints in this provider changed as a result of this call
     */
    protected final boolean add(ConstraintDefinition<Score_> constraintDefinition) {
        if (!filter(constraintDefinition)) {
            return false;
        }
        return constraintDefinitionSet.add(constraintDefinition);
    }

    private Stream<ConstraintDefinition<Score_>> getConstraintDescriptors() {
        return constraintDefinitionSet.stream();
    }

    /**
     * Determines whether a constraint descriptor
     * (supplied via {@link #add(ConstraintDefinition)} or {@link #addAll(ComposableConstraintProvider)})
     * should be added into this provider.
     * Unless overridden by a subclass, all constraint descriptors are accepted.
     *
     * @param constraintDefinition the constraint descriptor to check
     * @return true if the constraint descriptor should be added into this provider
     */
    protected boolean filter(ConstraintDefinition<Score_> constraintDefinition) {
        return true;
    }

    @Override
    public final Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return getConstraintDescriptors()
                .map(constraintDescriptor -> assemble(constraintDescriptor, constraintFactory))
                .toArray(Constraint[]::new);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static <Score_ extends Score<Score_>> Constraint assemble(ConstraintDefinition<Score_> constraintDefinition,
            ConstraintFactory constraintFactory) {
        var builder = constraintDefinition.buildConstraint(constraintFactory)
                .usingDefaultConstraintWeight(constraintDefinition.defaultConstraintWeight());
        if (builder instanceof UniConstraintBuilder uniBuilder) {
            var uniConstraintDescriptor = (UniConstraintDefinition) constraintDefinition;
            if (uniConstraintDescriptor.justificationFunction() != null) {
                uniBuilder = uniBuilder.justifyWith(uniConstraintDescriptor.justificationFunction());
            }
            if (constraintDefinition.indictmentFunction() != null) {
                uniBuilder = uniBuilder.indictWith(uniConstraintDescriptor.indictmentFunction());
            }
            return uniBuilder.asConstraintDescribed(constraintDefinition.name(),
                    constraintDefinition.description(), constraintDefinition.group());
        } else if (builder instanceof BiConstraintBuilder biBuilder) {
            var biConstraintDescriptor = (BiConstraintDefinition) constraintDefinition;
            if (constraintDefinition.justificationFunction() != null) {
                biBuilder = biBuilder.justifyWith(biConstraintDescriptor.justificationFunction());
            }
            if (constraintDefinition.indictmentFunction() != null) {
                biBuilder = biBuilder.indictWith(biConstraintDescriptor.indictmentFunction());
            }
            return biBuilder.asConstraintDescribed(constraintDefinition.name(),
                    constraintDefinition.description(), constraintDefinition.group());
        } else if (builder instanceof TriConstraintBuilder triBuilder) {
            var triConstraintDescriptor = (TriConstraintDefinition) constraintDefinition;
            if (triConstraintDescriptor.justificationFunction() != null) {
                triBuilder = triBuilder.justifyWith(triConstraintDescriptor.justificationFunction());
            }
            if (constraintDefinition.indictmentFunction() != null) {
                triBuilder = triBuilder.indictWith(triConstraintDescriptor.indictmentFunction());
            }
            return triBuilder.asConstraintDescribed(constraintDefinition.name(),
                    constraintDefinition.description(), constraintDefinition.group());
        } else if (builder instanceof QuadConstraintBuilder quadBuilder) {
            var quadConstraintDescriptor = (QuadConstraintDefinition) constraintDefinition;
            if (quadConstraintDescriptor.justificationFunction() != null) {
                quadBuilder = quadBuilder.justifyWith(quadConstraintDescriptor.justificationFunction());
            }
            if (constraintDefinition.indictmentFunction() != null) {
                quadBuilder = quadBuilder.indictWith(quadConstraintDescriptor.indictmentFunction());
            }
            return quadBuilder.asConstraintDescribed(constraintDefinition.name(),
                    constraintDefinition.description(), constraintDefinition.group());
        } else {
            throw new UnsupportedOperationException("Impossible state: Unsupported constraint cardinality: " + builder);
        }
    }

}
