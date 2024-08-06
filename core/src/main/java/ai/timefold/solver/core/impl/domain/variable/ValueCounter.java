package ai.timefold.solver.core.impl.domain.variable;

public sealed interface ValueCounter<Solution_>
        permits FromEntityValueCounter, FromSolutionValueCounter {

    void resetWorkingSolution(Solution_ workingSolution);

    void addEntity(Object entity);

    void removeEntity(Object entity);

    int getCount();

}
