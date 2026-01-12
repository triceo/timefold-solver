package ai.timefold.solver.core.impl.score.director;

import java.util.function.Consumer;

import ai.timefold.solver.core.impl.neighborhood.NeighborhoodsBasedMoveRepository;

public final class NeighborhoodNotifier<Solution_> implements Consumer<Object> {
    private boolean isTracking;
    private NeighborhoodsBasedMoveRepository<Solution_> moveRepository;

    public NeighborhoodNotifier() {
        isTracking = false;
    }

    public void setTracking(boolean isTracking) {
        this.isTracking = isTracking;
    }

    public NeighborhoodsBasedMoveRepository<Solution_> getMoveRepository() {
        return moveRepository;
    }

    public void setMoveRepository(NeighborhoodsBasedMoveRepository<Solution_> moveRepository) {
        this.moveRepository = moveRepository;
    }

    @Override
    public void accept(Object entity) {
        if (moveRepository == null) {
            return;
        }
        if (isTracking) {
            moveRepository.update(entity);
        }
    }
}
