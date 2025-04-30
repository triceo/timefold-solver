package ai.timefold.solver.core.api.solver;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import ai.timefold.solver.core.impl.util.MathUtils;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ProblemSizeStatisticsTest {

    private static ProblemSizeStatistics getProblemSizeStatistics(long scale) {
        return new ProblemSizeStatistics(0L, 0L, 0L,
                Math.log10(scale));
    }

    private static final Locale defaultLocaleToRestore = Locale.getDefault();

    @BeforeAll
    public static synchronized void setLocale() {
        Locale.setDefault(Locale.US);
    }

    @AfterAll
    public static synchronized void restoreLocale() {
        Locale.setDefault(defaultLocaleToRestore);
    }

    @Test
    void getApproximateProblemScaleLogAsFixedPointLong() {
        var statistics = getProblemSizeStatistics(100L);
        assertThat(statistics.approximateProblemScaleLogAsFixedPointLong())
                .isEqualTo(MathUtils.getScaledApproximateLog(MathUtils.LOG_PRECISION, 10L, 100L));

        statistics = getProblemSizeStatistics(250L);
        assertThat(statistics.approximateProblemScaleLogAsFixedPointLong())
                .isEqualTo(MathUtils.getScaledApproximateLog(MathUtils.LOG_PRECISION, 10L, 250L));
    }

    @Test
    void formatApproximateProblemScaleLocaled() {
        var statistics = getProblemSizeStatistics(100L);
        assertThat(statistics.approximateProblemScaleAsFormattedString(Locale.FRENCH))
                .isEqualTo("100");

        statistics = getProblemSizeStatistics(250L);
        assertThat(statistics.approximateProblemScaleAsFormattedString(Locale.FRENCH))
                .isEqualTo("250");

        statistics = getProblemSizeStatistics(1_234_567L);
        assertThat(statistics.approximateProblemScaleAsFormattedString(Locale.FRENCH))
                .isEqualTo("1 234 567");

        statistics = getProblemSizeStatistics(123_456_789L);
        assertThat(statistics.approximateProblemScaleAsFormattedString(Locale.FRENCH))
                .isEqualTo("123 456 789");

        statistics = getProblemSizeStatistics(1_123_456_789L);
        assertThat(statistics.approximateProblemScaleAsFormattedString(Locale.FRENCH))
                .isEqualTo("1 123 456 789");

        statistics = getProblemSizeStatistics(321_123_456_789L);
        assertThat(statistics.approximateProblemScaleAsFormattedString(Locale.FRENCH))
                .isEqualTo("3,211235 × 10^11");

        // scale = -infinity
        statistics = new ProblemSizeStatistics(0L, 0L, 0L, Double.NEGATIVE_INFINITY);
        assertThat(statistics.approximateProblemScaleAsFormattedString(Locale.FRENCH))
                .isEqualTo("0");

        // scale = +infinity
        statistics = new ProblemSizeStatistics(0L, 0L, 0L, Double.POSITIVE_INFINITY);
        assertThat(statistics.approximateProblemScaleAsFormattedString(Locale.FRENCH))
                .isEqualTo("0");

        // scale = NaN
        statistics = new ProblemSizeStatistics(0L, 0L, 0L, Double.NaN);
        assertThat(statistics.approximateProblemScaleAsFormattedString(Locale.FRENCH))
                .isEqualTo("0");

        // scale = 0
        statistics = new ProblemSizeStatistics(0L, 0L, 0L, 0);
        assertThat(statistics.approximateProblemScaleAsFormattedString(Locale.FRENCH))
                .isEqualTo("1");
    }

    @Test
    void formatApproximateProblemScale() {
        var statistics = getProblemSizeStatistics(100L);
        assertThat(statistics.approximateProblemScaleAsFormattedString())
                .isEqualTo("100");

        statistics = getProblemSizeStatistics(250L);
        assertThat(statistics.approximateProblemScaleAsFormattedString())
                .isEqualTo("250");

        statistics = getProblemSizeStatistics(1_234_567L);
        assertThat(statistics.approximateProblemScaleAsFormattedString())
                .isEqualTo("1,234,567");

        statistics = getProblemSizeStatistics(123_456_789L);
        assertThat(statistics.approximateProblemScaleAsFormattedString())
                .isEqualTo("123,456,789");

        statistics = getProblemSizeStatistics(1_123_456_789L);
        assertThat(statistics.approximateProblemScaleAsFormattedString())
                .isEqualTo("1,123,456,789");

        statistics = getProblemSizeStatistics(321_123_456_789L);
        assertThat(statistics.approximateProblemScaleAsFormattedString())
                .isEqualTo("3.211235 × 10^11");

        // scale = -infinity
        statistics = new ProblemSizeStatistics(0L, 0L, 0L, Double.NEGATIVE_INFINITY);
        assertThat(statistics.approximateProblemScaleAsFormattedString())
                .isEqualTo("0");

        // scale = +infinity
        statistics = new ProblemSizeStatistics(0L, 0L, 0L, Double.POSITIVE_INFINITY);
        assertThat(statistics.approximateProblemScaleAsFormattedString())
                .isEqualTo("0");

        // scale = NaN
        statistics = new ProblemSizeStatistics(0L, 0L, 0L, Double.NaN);
        assertThat(statistics.approximateProblemScaleAsFormattedString())
                .isEqualTo("0");

        // scale = 0
        statistics = new ProblemSizeStatistics(0L, 0L, 0L, 0);
        assertThat(statistics.approximateProblemScaleAsFormattedString())
                .isEqualTo("1");
    }

}
