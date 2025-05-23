package ai.timefold.solver.core.impl.exhaustivesearch.node.bounder;

import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.director.ScoreDirector;
import ai.timefold.solver.core.impl.score.definition.ScoreDefinition;
import ai.timefold.solver.core.impl.score.director.InnerScore;
import ai.timefold.solver.core.impl.score.trend.InitializingScoreTrend;

public interface ScoreBounder<Score_ extends Score<Score_>> {

    /**
     * In OR terms, this is called the lower bound if they minimize, and upper bound if they maximize.
     * Because we always maximize the {@link Score}, calling it lower bound would be a contradiction.
     *
     * @param scoreDirector never null, use {@link ScoreDirector#getWorkingSolution()} to get the working
     *        {@link PlanningSolution}
     * @param score never null, the {@link Score} of the working {@link PlanningSolution}
     * @return never null, never worse than the best possible {@link Score} we can get
     *         by initializing the uninitialized variables of the working {@link PlanningSolution}.
     * @see ScoreDefinition#buildOptimisticBound(InitializingScoreTrend, Score)
     */
    InnerScore<Score_> calculateOptimisticBound(ScoreDirector<?> scoreDirector, InnerScore<Score_> score);

    /**
     * In OR terms, this is called the upper bound if they minimize, and lower bound if they maximize.
     * Because we always maximize the {@link Score}, calling it upper bound would be a contradiction.
     *
     * @param scoreDirector never null, use {@link ScoreDirector#getWorkingSolution()} to get the working
     *        {@link PlanningSolution}
     * @param score never null, the {@link Score} of the working {@link PlanningSolution}
     * @return never null, never better than the worst possible {@link Score} we can get
     *         by initializing the uninitialized variables of the working {@link PlanningSolution}.
     * @see ScoreDefinition#buildPessimisticBound(InitializingScoreTrend, Score)
     */
    InnerScore<Score_> calculatePessimisticBound(ScoreDirector<?> scoreDirector, InnerScore<Score_> score);

}
