package ai.timefold.solver.core.api.function;

import java.util.Objects;
import java.util.function.Predicate;

import org.jspecify.annotations.NonNull;

/**
 * Represents a predicate (boolean-valued function) of three arguments.
 * This is the three-arity specialization of {@link Predicate}.
 *
 * <p>
 * This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #test(Object, Object, Object)}.
 *
 * @param <A> the type of the first argument to the predicate
 * @param <B> the type of the second argument the predicate
 * @param <C> the type of the third argument the predicate
 *
 * @see Predicate
 */
@FunctionalInterface
public interface TriPredicate<A, B, C> {

    /**
     * Evaluates this predicate on the given arguments.
     *
     * @param a the first input argument
     * @param b the second input argument
     * @param c the third input argument
     * @return {@code true} if the input arguments match the predicate,
     *         otherwise {@code false}
     */
    boolean test(A a, B b, C c);

    /**
     * Returns a composed predicate that represents a short-circuiting logical
     * AND of this predicate and another. When evaluating the composed
     * predicate, if this predicate is {@code false}, then the {@code other}
     * predicate is not evaluated.
     *
     * <p>
     * Any exceptions thrown during evaluation of either predicate are relayed
     * to the caller; if evaluation of this predicate throws an exception, the
     * {@code other} predicate will not be evaluated.
     *
     * @param other a predicate that will be logically-ANDed with this predicate
     * @return a composed predicate that represents the short-circuiting logical
     *         AND of this predicate and the {@code other} predicate
     * @throws NullPointerException if other is null
     */
    default @NonNull TriPredicate<A, B, C> and(@NonNull TriPredicate<? super A, ? super B, ? super C> other) {
        Objects.requireNonNull(other);
        return (A a, B b, C c) -> test(a, b, c) && other.test(a, b, c);
    }

    /**
     * Returns a predicate that represents the logical negation of this
     * predicate.
     *
     * @return a predicate that represents the logical negation of this
     *         predicate
     */
    default TriPredicate<A, B, C> negate() {
        return (A a, B b, C c) -> !test(a, b, c);
    }

    /**
     * Returns a composed predicate that represents a short-circuiting logical
     * OR of this predicate and another. When evaluating the composed
     * predicate, if this predicate is {@code true}, then the {@code other}
     * predicate is not evaluated.
     *
     * <p>
     * Any exceptions thrown during evaluation of either predicate are relayed
     * to the caller; if evaluation of this predicate throws an exception, the
     * {@code other} predicate will not be evaluated.
     *
     * @param other a predicate that will be logically-ORed with this predicate
     * @return a composed predicate that represents the short-circuiting logical
     *         OR of this predicate and the {@code other} predicate
     * @throws NullPointerException if other is null
     */
    default @NonNull TriPredicate<A, B, C> or(@NonNull TriPredicate<? super A, ? super B, ? super C> other) {
        Objects.requireNonNull(other);
        return (A a, B b, C c) -> test(a, b, c) || other.test(a, b, c);
    }
}
