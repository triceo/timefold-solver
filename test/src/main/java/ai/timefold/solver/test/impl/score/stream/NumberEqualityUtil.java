package ai.timefold.solver.test.impl.score.stream;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.function.BiPredicate;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.impl.score.definition.ScoreDefinition;

final class NumberEqualityUtil {

    /**
     * Return the correct predicate to compare two unknown subtypes of {@link Number}.
     * Supports {@link Integer}, {@link Long} and {@link BigDecimal}.
     * If two numbers are not equal but still represent the same point on the number line
     * (such as int 1, long 1 and {@link BigDecimal#ONE}
     * the predicate will accept.
     *
     * @param scoreDefinition never null; will determine the type of number we receive from the score
     * @param expectedImpact never null; user-provided type to check that number against
     * @return never null
     * @param <Score_>
     */
    public static <Score_ extends Score<Score_>> BiPredicate<Number, Number>
            getEqualityPredicate(ScoreDefinition<Score_> scoreDefinition, Number expectedImpact) {
        if (expectedImpact instanceof Integer) {
            return getIntEqualityPredicate(scoreDefinition);
        } else if (expectedImpact instanceof Long) {
            return getLongEqualityPredicate(scoreDefinition);
        } else if (expectedImpact instanceof BigDecimal) {
            return getBigDecimalEqualityPredicate(scoreDefinition);
        } else {
            throw new IllegalStateException("Impossible state: unknown impact type class (" + expectedImpact.getClass()
                    + ") for impact (" + expectedImpact + ").");
        }
    }

    private static <Score_ extends Score<Score_>> BiPredicate<Number, Number>
            getIntEqualityPredicate(ScoreDefinition<Score_> scoreDefinition) {
        Class<?> actualImpactType = scoreDefinition.getNumericType();
        if (actualImpactType == int.class) {
            return (Number expected, Number actual) -> expected.intValue() == actual.intValue();
        } else if (actualImpactType == long.class) {
            return (Number expected, Number actual) -> expected.longValue() == actual.longValue();
        } else if (actualImpactType == BigDecimal.class) {
            return (Number expected,
                    Number actual) -> (BigDecimal.valueOf(expected.intValue())).compareTo((BigDecimal) actual) == 0;
        } else {
            throw new IllegalStateException("Impossible state: unknown numeric type (" + actualImpactType
                    + ") for score definition (" + scoreDefinition.getClass() + ").");
        }
    }

    private static <Score_ extends Score<Score_>> BiPredicate<Number, Number>
            getLongEqualityPredicate(ScoreDefinition<Score_> scoreDefinition) {
        Class<?> actualImpactType = scoreDefinition.getNumericType();
        if (actualImpactType == int.class) {
            return (Number expected, Number actual) -> expected.longValue() == actual.intValue();
        } else if (actualImpactType == long.class) {
            return (Number expected, Number actual) -> expected.longValue() == actual.longValue();
        } else if (actualImpactType == BigDecimal.class) {
            return (Number expected,
                    Number actual) -> (BigDecimal.valueOf(expected.longValue())).compareTo((BigDecimal) actual) == 0;
        } else {
            throw new IllegalStateException("Impossible state: unknown numeric type (" + actualImpactType
                    + ") for score definition (" + scoreDefinition.getClass() + ").");
        }
    }

    private static <Score_ extends Score<Score_>> BiPredicate<Number, Number>
            getBigDecimalEqualityPredicate(ScoreDefinition<Score_> scoreDefinition) {
        Class<?> actualImpactType = scoreDefinition.getNumericType();
        if (actualImpactType == int.class) {
            return (Number expected,
                    Number actual) -> ((BigDecimal) expected).compareTo(BigDecimal.valueOf(actual.intValue())) == 0;
        } else if (actualImpactType == long.class) {
            return (Number expected,
                    Number actual) -> ((BigDecimal) expected).compareTo(BigDecimal.valueOf(actual.longValue())) == 0;
        } else if (actualImpactType == BigDecimal.class) {
            return (Number expected, Number actual) -> ((BigDecimal) expected).compareTo((BigDecimal) actual) == 0;
        } else {
            throw new IllegalStateException("Impossible state: unknown numeric type (" + actualImpactType
                    + ") for score definition (" + scoreDefinition.getClass() + ").");
        }
    }

    /**
     * Return the correct predicate to compare two unknown subtypes of {@link Number}.
     * Supports {@link Integer}, {@link Long} and {@link BigDecimal}.
     * If two numbers are not equal but still represent the same point on the number line
     * (such as int 1, long 1 and {@link BigDecimal#ONE}
     * the predicate will accept.
     *
     * @param expectedImpact never null; user-provided type to check that number against
     * @return never null
     */
    public static Comparator<Number> getComparison(Number expectedImpact) {
        return Comparator.comparing(a -> {
            if (a instanceof BigDecimal bigDecimal) {
                return bigDecimal;
            } else {
                return BigDecimal.valueOf(a.longValue());
            }
        });
    }

    private NumberEqualityUtil() {
        // No external instances.
    }

}
