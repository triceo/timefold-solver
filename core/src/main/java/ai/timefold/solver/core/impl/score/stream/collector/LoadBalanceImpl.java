package ai.timefold.solver.core.impl.score.stream.collector;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Map;

import ai.timefold.solver.core.api.score.stream.common.LoadBalance;

import org.jspecify.annotations.NonNull;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;

public final class LoadBalanceImpl<Balanced_> implements LoadBalance<Balanced_> {

    // The default count for both maps is zero.
    private static final int DEFAULT_RETURN_VALUE = 0;
    // If need be, precision can be made configurable on the constraint collector level.
    private static final MathContext RESULT_MATH_CONTEXT = new MathContext(6, RoundingMode.HALF_EVEN);

    private final Object2IntOpenHashMap<Balanced_> balancedItemCountMap = new Object2IntOpenHashMap<>();
    private final Object2LongLinkedOpenHashMap<Balanced_> balancedItemToMetricValueMap = new Object2LongLinkedOpenHashMap<>();

    private long sum = 0;
    private long squaredDeviationIntegralPart = 0;
    private long squaredDeviationFractionNumerator = 0;

    public LoadBalanceImpl() {
        balancedItemCountMap.defaultReturnValue(DEFAULT_RETURN_VALUE);
        balancedItemToMetricValueMap.defaultReturnValue(DEFAULT_RETURN_VALUE);
    }

    public Runnable registerBalanced(Balanced_ balanced, long metricValue, long initialMetricValue) {
        var balancedItemCount = balancedItemCountMap.addTo(balanced, 1);
        if (balancedItemCount == 0) {
            addToMetric(balanced, metricValue + initialMetricValue);
        } else {
            addToMetric(balanced, metricValue);
        }
        return () -> unregisterBalanced(balanced, metricValue);
    }

    public void unregisterBalanced(Balanced_ balanced, long metricValue) {
        var oldCount = balancedItemCountMap.addTo(balanced, -1);
        if (oldCount == 1) {
            balancedItemCountMap.removeInt(balanced);
            resetMetric(balanced);
        } else {
            addToMetric(balanced, -metricValue);
        }
    }

    private void addToMetric(Balanced_ balanced, long diff) {
        var oldValue = balancedItemToMetricValueMap.addTo(balanced, diff);
        var newValue = oldValue + diff;
        if (oldValue != newValue) {
            updateSquaredDeviation(oldValue, newValue);
            sum += diff;
        }
    }

    private void resetMetric(Balanced_ balanced) {
        var oldValue = balancedItemToMetricValueMap.removeLong(balanced);
        if (oldValue != 0) {
            updateSquaredDeviation(oldValue, 0);
            sum -= oldValue;
        }
    }

    private void updateSquaredDeviation(long oldValue, long newValue) {
        // o' = o + (x_0'^2 - x_0^2) + (2 * (x_0s - x_0's') + 2 * (x_1 + x_2 + x_3 + ... + x_n)(s - s') + (s'^2 - s^2))/n

        //(x_0'^2 - x_0^2)
        var squaredDeviationFirstTerm = newValue * newValue - oldValue * oldValue;

        // 2 * (x_1 + x_2 + x_3 + ... + x_n)
        var secondTermFirstFactor = 2 * (sum - oldValue);

        var newSum = sum - oldValue + newValue;

        // (s' - s)
        var secondTermSecondFactor = sum - newSum;

        // (s'^2 - s^2)
        var thirdTerm = newSum * newSum - sum * sum;

        // 2 * (x_0u - x_0'u')
        var fourthTerm = 2 * (oldValue * sum - newValue * newSum);
        var squaredDeviationSecondTermNumerator = secondTermFirstFactor * secondTermSecondFactor + thirdTerm + fourthTerm;

        squaredDeviationIntegralPart += squaredDeviationFirstTerm;
        squaredDeviationFractionNumerator += squaredDeviationSecondTermNumerator;
    }

    @Override
    public @NonNull Map<Balanced_, Long> loads() {
        if (balancedItemCountMap.isEmpty()) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(balancedItemToMetricValueMap);
    }

    @Override
    public @NonNull BigDecimal unfairness() {
        var totalToBalanceCount = balancedItemCountMap.size();
        return switch (totalToBalanceCount) {
            case 0 -> BigDecimal.ZERO;
            case 1 -> BigDecimal.valueOf(squaredDeviationFractionNumerator + squaredDeviationIntegralPart)
                    .sqrt(RESULT_MATH_CONTEXT);
            default -> { // Only do the final sqrt as BigDecimal, fast floating point math is good enough for the rest.
                var tmp = (squaredDeviationFractionNumerator / (double) totalToBalanceCount) + squaredDeviationIntegralPart;
                yield BigDecimal.valueOf(tmp)
                        .sqrt(RESULT_MATH_CONTEXT);
            }
        };
    }

}
