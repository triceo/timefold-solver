package ai.timefold.solver.core.impl.neighborhood;

import org.jspecify.annotations.NullMarked;

// Not sealed, because we need a way to mock it in tests.
@NullMarked
public non-sealed interface NeighborhoodsBasedMoveRepository<Solution_> extends MoveRepository<Solution_> {

    void insert(Object planningEntityOrProblemFact);

    void update(Object planningEntityOrProblemFact);

    void retract(Object planningEntityOrProblemFact);

}
