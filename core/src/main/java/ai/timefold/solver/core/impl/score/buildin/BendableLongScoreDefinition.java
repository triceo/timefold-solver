package ai.timefold.solver.core.impl.score.buildin;

import java.util.Arrays;
import java.util.stream.LongStream;

import ai.timefold.solver.core.api.score.buildin.bendablelong.BendableLongScore;
import ai.timefold.solver.core.config.score.trend.InitializingScoreTrendLevel;
import ai.timefold.solver.core.impl.score.definition.AbstractBendableScoreDefinition;
import ai.timefold.solver.core.impl.score.trend.InitializingScoreTrend;

public class BendableLongScoreDefinition extends AbstractBendableScoreDefinition<BendableLongScore> {

    public BendableLongScoreDefinition(int hardLevelsSize, int softLevelsSize) {
        super(hardLevelsSize, softLevelsSize);
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public Class<BendableLongScore> getScoreClass() {
        return BendableLongScore.class;
    }

    @Override
    public BendableLongScore getZeroScore() {
        return BendableLongScore.zero(hardLevelsSize, softLevelsSize);
    }

    @Override
    public final BendableLongScore getOneSoftestScore() {
        return BendableLongScore.ofSoft(hardLevelsSize, softLevelsSize, softLevelsSize - 1, 1L);
    }

    @Override
    public BendableLongScore parseScore(String scoreString) {
        BendableLongScore score = BendableLongScore.parseScore(scoreString);
        if (score.hardLevelsSize() != hardLevelsSize) {
            throw new IllegalArgumentException("The scoreString (" + scoreString
                    + ") for the scoreClass (" + BendableLongScore.class.getSimpleName()
                    + ") doesn't follow the correct pattern:"
                    + " the hardLevelsSize (" + score.hardLevelsSize()
                    + ") doesn't match the scoreDefinition's hardLevelsSize (" + hardLevelsSize + ").");
        }
        if (score.softLevelsSize() != softLevelsSize) {
            throw new IllegalArgumentException("The scoreString (" + scoreString
                    + ") for the scoreClass (" + BendableLongScore.class.getSimpleName()
                    + ") doesn't follow the correct pattern:"
                    + " the softLevelsSize (" + score.softLevelsSize()
                    + ") doesn't match the scoreDefinition's softLevelsSize (" + softLevelsSize + ").");
        }
        return score;
    }

    @Override
    public BendableLongScore fromLevelNumbers(int initScore, Number[] levelNumbers) {
        if (levelNumbers.length != getLevelsSize()) {
            throw new IllegalStateException("The levelNumbers (" + Arrays.toString(levelNumbers)
                    + ")'s length (" + levelNumbers.length + ") must equal the levelSize (" + getLevelsSize() + ").");
        }
        long[] hardScores = new long[hardLevelsSize];
        for (int i = 0; i < hardLevelsSize; i++) {
            hardScores[i] = (Long) levelNumbers[i];
        }
        long[] softScores = new long[softLevelsSize];
        for (int i = 0; i < softLevelsSize; i++) {
            softScores[i] = (Long) levelNumbers[hardLevelsSize + i];
        }
        return BendableLongScore.ofUninitialized(initScore, hardScores, softScores);
    }

    public BendableLongScore createScore(long... scores) {
        return createScoreUninitialized(0, scores);
    }

    public BendableLongScore createScoreUninitialized(int initScore, long... scores) {
        int levelsSize = hardLevelsSize + softLevelsSize;
        if (scores.length != levelsSize) {
            throw new IllegalArgumentException("The scores (" + Arrays.toString(scores)
                    + ")'s length (" + scores.length
                    + ") is not levelsSize (" + levelsSize + ").");
        }
        return BendableLongScore.ofUninitialized(initScore,
                Arrays.copyOfRange(scores, 0, hardLevelsSize),
                Arrays.copyOfRange(scores, hardLevelsSize, levelsSize));
    }

    @Override
    public BendableLongScore buildOptimisticBound(InitializingScoreTrend initializingScoreTrend,
            BendableLongScore score) {
        InitializingScoreTrendLevel[] trendLevels = initializingScoreTrend.trendLevels();
        long[] hardScores = new long[hardLevelsSize];
        for (int i = 0; i < hardLevelsSize; i++) {
            hardScores[i] = (trendLevels[i] == InitializingScoreTrendLevel.ONLY_DOWN)
                    ? score.hardScore(i)
                    : Long.MAX_VALUE;
        }
        long[] softScores = new long[softLevelsSize];
        for (int i = 0; i < softLevelsSize; i++) {
            softScores[i] = (trendLevels[hardLevelsSize + i] == InitializingScoreTrendLevel.ONLY_DOWN)
                    ? score.softScore(i)
                    : Long.MAX_VALUE;
        }
        return BendableLongScore.ofUninitialized(0, hardScores, softScores);
    }

    @Override
    public BendableLongScore buildPessimisticBound(InitializingScoreTrend initializingScoreTrend,
            BendableLongScore score) {
        InitializingScoreTrendLevel[] trendLevels = initializingScoreTrend.trendLevels();
        long[] hardScores = new long[hardLevelsSize];
        for (int i = 0; i < hardLevelsSize; i++) {
            hardScores[i] = (trendLevels[i] == InitializingScoreTrendLevel.ONLY_UP)
                    ? score.hardScore(i)
                    : Long.MIN_VALUE;
        }
        long[] softScores = new long[softLevelsSize];
        for (int i = 0; i < softLevelsSize; i++) {
            softScores[i] = (trendLevels[hardLevelsSize + i] == InitializingScoreTrendLevel.ONLY_UP)
                    ? score.softScore(i)
                    : Long.MIN_VALUE;
        }
        return BendableLongScore.ofUninitialized(0, hardScores, softScores);
    }

    @Override
    public BendableLongScore divideBySanitizedDivisor(BendableLongScore dividend, BendableLongScore divisor) {
        int dividendInitScore = dividend.initScore();
        int divisorInitScore = sanitize(divisor.initScore());
        long[] hardScores = new long[hardLevelsSize];
        for (int i = 0; i < hardLevelsSize; i++) {
            hardScores[i] = divide(dividend.hardScore(i), sanitize(divisor.hardScore(i)));
        }
        long[] softScores = new long[softLevelsSize];
        for (int i = 0; i < softLevelsSize; i++) {
            softScores[i] = divide(dividend.softScore(i), sanitize(divisor.softScore(i)));
        }
        long[] levels = LongStream.concat(Arrays.stream(hardScores), Arrays.stream(softScores)).toArray();
        return createScoreUninitialized(divide(dividendInitScore, divisorInitScore), levels);
    }

    @Override
    public Class<?> getNumericType() {
        return long.class;
    }
}
