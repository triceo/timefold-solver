package ai.timefold.solver.core.impl.move.streams.dataset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import ai.timefold.solver.core.impl.domain.variable.supply.SupplyManager;
import ai.timefold.solver.core.impl.testdata.domain.TestdataEntity;
import ai.timefold.solver.core.impl.testdata.domain.TestdataSolution;
import ai.timefold.solver.core.impl.testdata.domain.list.TestdataListEntity;
import ai.timefold.solver.core.impl.testdata.domain.list.TestdataListSolution;

import org.junit.jupiter.api.Test;

class UniDatasetStreamTest {

    @Test
    void forEachBasicVariable() {
        var dataStreamFactory = new DataStreamFactory<>(TestdataSolution.buildSolutionDescriptor());
        var uniDataset = ((AbstractUniDataStream<TestdataSolution, TestdataEntity>) dataStreamFactory
                .forEachIncludingPinned(TestdataEntity.class))
                .createDataset();

        var supplyManager = mock(SupplyManager.class);
        var solution = TestdataSolution.generateSolution(2, 2);
        var datasetSession = UniDatasetStreamTest.createSession(dataStreamFactory, solution, supplyManager);
        var uniDatasetInstance = datasetSession.getInstance(uniDataset);

        var entity1 = solution.getEntityList().get(0);
        var entity2 = solution.getEntityList().get(1);

        assertThat(uniDatasetInstance.iterator())
                .toIterable()
                .map(t -> t.factA)
                .containsExactly(entity1, entity2);

        // Make incremental changes.
        var entity3 = new TestdataEntity("entity3", solution.getValueList().get(0));
        datasetSession.insert(entity3);
        datasetSession.retract(entity2);
        datasetSession.settle();

        assertThat(uniDatasetInstance.iterator())
                .toIterable()
                .map(t -> t.factA)
                .containsExactly(entity1, entity3);
    }

    @Test
    void forEachListVariable() {
        var dataStreamFactory = new DataStreamFactory<>(TestdataListSolution.buildSolutionDescriptor());
        var uniDataset = ((AbstractUniDataStream<TestdataListSolution, TestdataListEntity>) dataStreamFactory
                .forEachIncludingPinned(TestdataListEntity.class))
                .createDataset();

        var supplyManager = mock(SupplyManager.class);
        var solution = TestdataListSolution.generateInitializedSolution(2, 2);
        var datasetSession = createSession(dataStreamFactory, solution, supplyManager);
        var uniDatasetInstance = datasetSession.getInstance(uniDataset);

        var entity1 = solution.getEntityList().get(0);
        var entity2 = solution.getEntityList().get(1);

        assertThat(uniDatasetInstance.iterator())
                .toIterable()
                .map(t -> t.factA)
                .containsExactly(entity1, entity2);

        // Make incremental changes.
        var entity3 = new TestdataListEntity("entity3");
        datasetSession.insert(entity3);
        datasetSession.retract(entity2);
        datasetSession.settle();

        assertThat(uniDatasetInstance.iterator())
                .toIterable()
                .map(t -> t.factA)
                .containsExactly(entity1, entity3);
    }

    private static <Solution_> DatasetSession<Solution_> createSession(DataStreamFactory<Solution_> dataStreamFactory,
            Solution_ solution, SupplyManager supplyManager) {
        var datasetSessionFactory = new DatasetSessionFactory<>(dataStreamFactory);
        var datasetSession = datasetSessionFactory.buildSession();
        datasetSession.initialize(solution, supplyManager);

        var solutionDescriptor = dataStreamFactory.getSolutionDescriptor();
        solutionDescriptor.visitAll(solution, datasetSession::insert);

        datasetSession.settle();
        return datasetSession;
    }

}
