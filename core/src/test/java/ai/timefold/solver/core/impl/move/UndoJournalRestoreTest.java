package ai.timefold.solver.core.impl.move;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import ai.timefold.solver.core.api.score.SimpleScore;
import ai.timefold.solver.core.api.score.calculator.EasyScoreCalculator;
import ai.timefold.solver.core.api.score.calculator.IncrementalScoreCalculator;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import ai.timefold.solver.core.config.score.trend.InitializingScoreTrendLevel;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.impl.AbstractFromConfigFactory;
import ai.timefold.solver.core.impl.constructionheuristic.DefaultConstructionHeuristicPhaseFactory;
import ai.timefold.solver.core.impl.domain.solution.descriptor.SolutionDescriptor;
import ai.timefold.solver.core.impl.domain.variable.ListVariableState;
import ai.timefold.solver.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import ai.timefold.solver.core.impl.heuristic.HeuristicConfigPolicy;
import ai.timefold.solver.core.impl.heuristic.selector.move.generic.RuinRecreateConstructionHeuristicPhaseBuilder;
import ai.timefold.solver.core.impl.heuristic.selector.move.generic.SelectorBasedRuinRecreateMove;
import ai.timefold.solver.core.impl.heuristic.selector.move.generic.list.ruin.SelectorBasedListRuinRecreateMove;
import ai.timefold.solver.core.impl.score.director.InnerScoreDirector;
import ai.timefold.solver.core.impl.score.director.easy.EasyScoreDirectorFactory;
import ai.timefold.solver.core.impl.score.director.incremental.IncrementalScoreDirectorFactory;
import ai.timefold.solver.core.impl.score.trend.InitializingScoreTrend;
import ai.timefold.solver.core.impl.solver.exception.UndoScoreCorruptionException;
import ai.timefold.solver.core.impl.solver.scope.SolverScope;
import ai.timefold.solver.core.preview.api.domain.metamodel.PlanningListVariableMetaModel;
import ai.timefold.solver.core.preview.api.move.Move;
import ai.timefold.solver.core.testdomain.TestdataEntity;
import ai.timefold.solver.core.testdomain.TestdataSolution;
import ai.timefold.solver.core.testdomain.TestdataValue;
import ai.timefold.solver.core.testdomain.cascade.single.TestdataSingleCascadingEntity;
import ai.timefold.solver.core.testdomain.cascade.single.TestdataSingleCascadingSolution;
import ai.timefold.solver.core.testdomain.cascade.single.TestdataSingleCascadingValue;
import ai.timefold.solver.core.testdomain.list.TestdataListEntity;
import ai.timefold.solver.core.testdomain.list.TestdataListSolution;
import ai.timefold.solver.core.testdomain.list.TestdataListValue;
import ai.timefold.solver.core.testdomain.list.pinned.index.TestdataPinnedWithIndexListEntity;
import ai.timefold.solver.core.testdomain.list.pinned.index.TestdataPinnedWithIndexListSolution;
import ai.timefold.solver.core.testdomain.list.pinned.index.TestdataPinnedWithIndexListValue;
import ai.timefold.solver.core.testdomain.list.shadowhistory.TestdataListEntityWithShadowHistory;
import ai.timefold.solver.core.testdomain.list.shadowhistory.TestdataListSolutionWithShadowHistory;
import ai.timefold.solver.core.testdomain.list.shadowhistory.TestdataListValueWithShadowHistory;
import ai.timefold.solver.core.testdomain.shadow.inverserelation.TestdataInverseRelationEntity;
import ai.timefold.solver.core.testdomain.shadow.inverserelation.TestdataInverseRelationSolution;
import ai.timefold.solver.core.testdomain.shadow.inverserelation.TestdataInverseRelationValue;
import ai.timefold.solver.core.testdomain.shadow.simple_list.TestdataDeclarativeSimpleListEntity;
import ai.timefold.solver.core.testdomain.shadow.simple_list.TestdataDeclarativeSimpleListSolution;
import ai.timefold.solver.core.testdomain.shadow.simple_list.TestdataDeclarativeSimpleListValue;

import org.junit.jupiter.api.Test;

/**
 * Temporary moves are undone by restoring the undo journal of the score director.
 * Each test compares the genuine values, the shadow values and the supply state with the start state.
 */
class UndoJournalRestoreTest {

    private static final SolutionDescriptor<TestdataListSolutionWithShadowHistory> HISTORY_DESCRIPTOR =
            TestdataListSolutionWithShadowHistory.buildSolutionDescriptor();
    private static final PlanningListVariableMetaModel<TestdataListSolutionWithShadowHistory, TestdataListEntityWithShadowHistory, TestdataListValueWithShadowHistory> HISTORY_LIST_VARIABLE =
            HISTORY_DESCRIPTOR.getMetaModel()
                    .genuineEntity(TestdataListEntityWithShadowHistory.class)
                    .listVariable("valueList", TestdataListValueWithShadowHistory.class);

    // ************************************************************************
    // List model with index, inverse, previous and next shadows
    // ************************************************************************

    @Test
    void listCrossMoveRestoresValuesAndCarrier() {
        var solution = historySolution(List.of(List.of("a0", "a1", "a2"), List.of("b0", "b1")), List.of());
        var scoreDirector = buildScoreDirector(HISTORY_DESCRIPTOR, solution, EnvironmentMode.PHASE_ASSERT);
        var a = solution.getEntityList().get(0);
        var b = solution.getEntityList().get(1);
        Move<TestdataListSolutionWithShadowHistory> move = view -> {
            view.moveValueBetweenLists(HISTORY_LIST_VARIABLE, a, 1, b, 0); // x: A -> B
            view.moveValueBetweenLists(HISTORY_LIST_VARIABLE, b, 2, a, 0); // y: B -> A
        };
        assertUndoRestoresHistoryModel(scoreDirector, solution, move);
    }

    @Test
    void listEntityChangedThreeTimesInOneMove() {
        var solution = historySolution(List.of(List.of("a0", "a1", "a2", "a3"), List.of("b0")), List.of());
        var scoreDirector = buildScoreDirector(HISTORY_DESCRIPTOR, solution, EnvironmentMode.PHASE_ASSERT);
        var a = solution.getEntityList().get(0);
        Move<TestdataListSolutionWithShadowHistory> move = view -> {
            view.moveValueInList(HISTORY_LIST_VARIABLE, a, 0, 3);
            view.moveValueInList(HISTORY_LIST_VARIABLE, a, 2, 0);
            view.moveValueInList(HISTORY_LIST_VARIABLE, a, 1, 2);
        };
        assertUndoRestoresHistoryModel(scoreDirector, solution, move);
    }

    @Test
    void listAssignAndUnassignRestoreStartState() {
        // allowsUnassignedValues=false: the init score counts the unassigned value u.
        var solution = historySolution(List.of(List.of("a0", "a1"), List.of("b0")), List.of("u"));
        var scoreDirector = buildScoreDirector(HISTORY_DESCRIPTOR, solution, EnvironmentMode.PHASE_ASSERT);
        var a = solution.getEntityList().get(0);
        var b = solution.getEntityList().get(1);
        var u = solution.getValueList().getLast();
        var startInitScore = scoreDirector.getWorkingInitScore();
        assertThat(startInitScore).isEqualTo(-1);
        Move<TestdataListSolutionWithShadowHistory> move = view -> {
            view.assignValueAndAdd(HISTORY_LIST_VARIABLE, u, b, 1);
            var a1 = view.unassignValue(HISTORY_LIST_VARIABLE, a, 1);
            view.assignValueAndAdd(HISTORY_LIST_VARIABLE, a1, b, 0);
        };
        assertUndoRestoresHistoryModel(scoreDirector, solution, move);
        assertThat(scoreDirector.getWorkingInitScore()).isEqualTo(startInitScore);
    }

    @Test
    void listUnassignThenReassignKeepsInitScoreAtMostZero() {
        var solution = historySolution(List.of(List.of("a0", "a1"), List.of("b0")), List.of());
        var scoreDirector = buildScoreDirector(HISTORY_DESCRIPTOR, solution, EnvironmentMode.PHASE_ASSERT);
        var a = solution.getEntityList().get(0);
        var b = solution.getEntityList().get(1);
        assertThat(scoreDirector.getWorkingInitScore()).isZero();
        Move<TestdataListSolutionWithShadowHistory> move = view -> {
            var a0 = view.unassignValue(HISTORY_LIST_VARIABLE, a, 0);
            view.assignValueAndAdd(HISTORY_LIST_VARIABLE, a0, b, 1);
        };
        // A wrong event order during undo makes the init score positive, which throws.
        assertUndoRestoresHistoryModel(scoreDirector, solution, move);
        assertThat(scoreDirector.getWorkingInitScore()).isZero();
    }

    /**
     * Also checks that undo does not calculate the shadows again:
     * each shadow setter call during undo writes back the old value of exactly one forward write, in reverse order.
     */
    private static void assertUndoRestoresHistoryModel(
            InnerScoreDirector<TestdataListSolutionWithShadowHistory, SimpleScore> scoreDirector,
            TestdataListSolutionWithShadowHistory solution, Move<TestdataListSolutionWithShadowHistory> move) {
        var listVariableDescriptor = HISTORY_DESCRIPTOR.getListVariableDescriptor();
        var startState = snapshot(HISTORY_DESCRIPTOR, solution);
        var startListState = listStateOf(scoreDirector, listVariableDescriptor, solution.getValueList());
        var startHistorySizes = historySizesOf(solution);
        var moveHistorySizes = new IdentityHashMap<TestdataListValueWithShadowHistory, int[]>();
        var moveState = scoreDirector.getMoveDirector().executeTemporary(move, s -> {
            moveHistorySizes.putAll(historySizesOf(s));
            return snapshot(HISTORY_DESCRIPTOR, s);
        }, false);
        assertThat(moveState).as("The move must change the solution.").isNotEqualTo(startState);

        assertSoftly(softly -> {
            softly.assertThat(snapshot(HISTORY_DESCRIPTOR, solution)).isEqualTo(startState);
            softly.assertThat(listStateOf(scoreDirector, listVariableDescriptor, solution.getValueList()))
                    .isEqualTo(startListState);
            for (var value : solution.getValueList()) {
                var histories = historiesOf(value);
                var startValues = Arrays.asList(value.getEntity(), value.getIndex(), value.getPrevious(), value.getNext());
                for (var i = 0; i < histories.size(); i++) {
                    var history = histories.get(i);
                    var forward = history.subList(startHistorySizes.get(value)[i], moveHistorySizes.get(value)[i]);
                    var undo = new ArrayList<Object>(history.subList(moveHistorySizes.get(value)[i], history.size()));
                    softly.assertThat(undo)
                            .as("Shadow writes during undo of value (%s), shadow #%d.", value, i)
                            .containsExactlyElementsOf(expectedUndoWrites(startValues.get(i), forward));
                }
            }
        });
    }

    private static List<Object> expectedUndoWrites(Object startValue, List<?> forwardWrites) {
        var oldValues = new ArrayList<Object>();
        if (!forwardWrites.isEmpty()) {
            oldValues.add(startValue);
            oldValues.addAll(forwardWrites.subList(0, forwardWrites.size() - 1));
        }
        return oldValues.reversed();
    }

    private static List<List<?>> historiesOf(TestdataListValueWithShadowHistory value) {
        return List.of(value.getEntityHistory(), value.getIndexHistory(), value.getPreviousHistory(),
                value.getNextHistory());
    }

    private static Map<TestdataListValueWithShadowHistory, int[]>
            historySizesOf(TestdataListSolutionWithShadowHistory solution) {
        var sizes = new IdentityHashMap<TestdataListValueWithShadowHistory, int[]>();
        for (var value : solution.getValueList()) {
            sizes.put(value, historiesOf(value).stream().mapToInt(List::size).toArray());
        }
        return sizes;
    }

    private static TestdataListSolutionWithShadowHistory historySolution(List<List<String>> valueCodeLists,
            List<String> unassignedValueCodes) {
        var valueList = new ArrayList<TestdataListValueWithShadowHistory>();
        var entityList = new ArrayList<TestdataListEntityWithShadowHistory>();
        for (var i = 0; i < valueCodeLists.size(); i++) {
            var entityValueList = new ArrayList<TestdataListValueWithShadowHistory>();
            for (var valueCode : valueCodeLists.get(i)) {
                var value = new TestdataListValueWithShadowHistory(valueCode);
                valueList.add(value);
                entityValueList.add(value);
            }
            entityList.add(new TestdataListEntityWithShadowHistory(String.valueOf((char) ('A' + i)), entityValueList));
        }
        for (var valueCode : unassignedValueCodes) {
            valueList.add(new TestdataListValueWithShadowHistory(valueCode));
        }
        var solution = new TestdataListSolutionWithShadowHistory();
        solution.setValueList(valueList);
        solution.setEntityList(entityList);
        return solution;
    }

    // ************************************************************************
    // Cascade, basic inverse, declarative and pinned models
    // ************************************************************************

    @Test
    void cascadeUpdateNotCalledDuringUndo() {
        var solutionDescriptor = TestdataSingleCascadingSolution.buildSolutionDescriptor();
        var listVariable = solutionDescriptor.getMetaModel()
                .genuineEntity(TestdataSingleCascadingEntity.class)
                .listVariable("valueList", TestdataSingleCascadingValue.class);
        var values = List.of(new TestdataSingleCascadingValue(1), new TestdataSingleCascadingValue(2),
                new TestdataSingleCascadingValue(3));
        var a = new TestdataSingleCascadingEntity("A", new ArrayList<>(values.subList(0, 2)));
        var b = new TestdataSingleCascadingEntity("B", new ArrayList<>(values.subList(2, 3)));
        var solution = new TestdataSingleCascadingSolution();
        solution.setValueList(values);
        solution.setEntityList(List.of(a, b));
        var scoreDirector = buildScoreDirector(solutionDescriptor, solution, EnvironmentMode.PHASE_ASSERT);
        var startState = snapshot(solutionDescriptor, solution);

        Move<TestdataSingleCascadingSolution> move = view -> {
            view.moveValueBetweenLists(listVariable, a, 0, b, 1);
            view.moveValueBetweenLists(listVariable, b, 0, a, 1);
        };
        var callCountsAfterMove = scoreDirector.getMoveDirector().executeTemporary(move,
                s -> s.getValueList().stream().map(TestdataSingleCascadingValue::getNumberOfCalls).toList(), false);

        assertSoftly(softly -> {
            softly.assertThat(snapshot(solutionDescriptor, solution)).isEqualTo(startState);
            softly.assertThat(values.stream().map(TestdataSingleCascadingValue::getNumberOfCalls).toList())
                    .as("The cascade method must not be called during undo.")
                    .isEqualTo(callCountsAfterMove);
        });
    }

    @Test
    void basicExternalizedInverseCollectionRestored() {
        var solutionDescriptor = TestdataInverseRelationSolution.buildSolutionDescriptor();
        var variable = solutionDescriptor.getMetaModel()
                .genuineEntity(TestdataInverseRelationEntity.class)
                .basicVariable("value", TestdataInverseRelationValue.class);
        var v1 = new TestdataInverseRelationValue("v1");
        var v2 = new TestdataInverseRelationValue("v2");
        var v3 = new TestdataInverseRelationValue("v3");
        var e1 = new TestdataInverseRelationEntity("e1", v1);
        var e2 = new TestdataInverseRelationEntity("e2", v1);
        var e3 = new TestdataInverseRelationEntity("e3", v2);
        var solution = new TestdataInverseRelationSolution("s");
        solution.setValueList(List.of(v1, v2, v3));
        solution.setEntityList(List.of(e1, e2, e3));
        var scoreDirector = buildScoreDirector(solutionDescriptor, solution, EnvironmentMode.PHASE_ASSERT);
        var basicVariableState = scoreDirector.getBasicVariableState(
                solutionDescriptor.findEntityDescriptorOrFail(TestdataInverseRelationEntity.class)
                        .getGenuineVariableDescriptor("value"));
        Function<TestdataInverseRelationValue, Set<Object>> inverseOf =
                value -> new HashSet<>(basicVariableState.getInverseCollection(value));
        var startState = snapshot(solutionDescriptor, solution);
        var startInverse = solution.getValueList().stream().map(inverseOf).toList();

        // e1 changes 3 times in one move.
        Move<TestdataInverseRelationSolution> move = view -> {
            view.changeVariable(variable, e1, v2);
            view.changeVariable(variable, e3, v1);
            view.changeVariable(variable, e1, v3);
            view.changeVariable(variable, e1, null);
        };
        scoreDirector.getMoveDirector().executeTemporary(move, score -> score);

        assertSoftly(softly -> {
            softly.assertThat(snapshot(solutionDescriptor, solution)).isEqualTo(startState);
            softly.assertThat(solution.getValueList().stream().map(inverseOf).toList()).isEqualTo(startInverse);
        });
    }

    @Test
    void basicNonExternalizedInverseSupplyRestored() {
        var solutionDescriptor = TestdataSolution.buildSolutionDescriptor();
        var variable = solutionDescriptor.getMetaModel()
                .genuineEntity(TestdataEntity.class)
                .basicVariable("value", TestdataValue.class);
        var v1 = new TestdataValue("v1");
        var v2 = new TestdataValue("v2");
        var e1 = new TestdataEntity("e1", v1);
        var e2 = new TestdataEntity("e2", v1);
        var e3 = new TestdataEntity("e3", v2);
        var solution = new TestdataSolution("s");
        solution.setValueList(List.of(v1, v2));
        solution.setEntityList(List.of(e1, e2, e3));
        var scoreDirector = buildScoreDirector(solutionDescriptor, solution, EnvironmentMode.PHASE_ASSERT);
        // No inverse shadow in this model: this demands the inverse supply.
        var basicVariableState = scoreDirector.getBasicVariableState(
                solutionDescriptor.findEntityDescriptorOrFail(TestdataEntity.class).getGenuineVariableDescriptor("value"));
        Function<TestdataValue, Set<Object>> inverseOf =
                value -> new HashSet<>(basicVariableState.getInverseCollection(value));
        var startState = snapshot(solutionDescriptor, solution);
        var startInverse = solution.getValueList().stream().map(inverseOf).toList();

        Move<TestdataSolution> move = view -> {
            view.changeVariable(variable, e1, v2);
            view.changeVariable(variable, e3, v1);
            view.changeVariable(variable, e1, v1);
            view.changeVariable(variable, e1, v2);
        };
        scoreDirector.getMoveDirector().executeTemporary(move, score -> score);

        assertSoftly(softly -> {
            softly.assertThat(snapshot(solutionDescriptor, solution)).isEqualTo(startState);
            softly.assertThat(solution.getValueList().stream().map(inverseOf).toList()).isEqualTo(startInverse);
        });
    }

    @Test
    void declarativeModelRestored() {
        var solutionDescriptor = TestdataDeclarativeSimpleListSolution.buildSolutionDescriptor();
        var listVariable = solutionDescriptor.getMetaModel()
                .genuineEntity(TestdataDeclarativeSimpleListEntity.class)
                .listVariable("values", TestdataDeclarativeSimpleListValue.class);
        var a = new TestdataDeclarativeSimpleListEntity("A", 0, 0);
        var b = new TestdataDeclarativeSimpleListEntity("B", 10, 100);
        var values = new ArrayList<TestdataDeclarativeSimpleListValue>();
        for (var i = 0; i < 5; i++) {
            values.add(new TestdataDeclarativeSimpleListValue("v" + i, i, 10 + i));
        }
        a.getValues().addAll(values.subList(0, 3));
        b.getValues().addAll(values.subList(3, 5));
        var solution = new TestdataDeclarativeSimpleListSolution(List.of(a, b), values);
        var scoreDirector = buildScoreDirector(solutionDescriptor, solution, EnvironmentMode.PHASE_ASSERT);
        var startState = snapshot(solutionDescriptor, solution);

        Move<TestdataDeclarativeSimpleListSolution> move = view -> {
            view.moveValueBetweenLists(listVariable, a, 0, b, 1);
            view.moveValueInList(listVariable, a, 0, 1);
            view.moveValueBetweenLists(listVariable, b, 0, a, 2);
        };
        var moveState = scoreDirector.getMoveDirector().executeTemporary(move,
                s -> snapshot(solutionDescriptor, s), false);

        assertThat(moveState).isNotEqualTo(startState);
        assertThat(snapshot(solutionDescriptor, solution)).isEqualTo(startState);
    }

    @Test
    void pinnedPrefixUndoDoesNotFail() {
        var solutionDescriptor = TestdataPinnedWithIndexListSolution.buildSolutionDescriptor();
        var listVariable = solutionDescriptor.getMetaModel()
                .genuineEntity(TestdataPinnedWithIndexListEntity.class)
                .listVariable("valueList", TestdataPinnedWithIndexListValue.class);
        var aPin = new TestdataPinnedWithIndexListValue("aPin");
        var a1 = new TestdataPinnedWithIndexListValue("a1");
        var a2 = new TestdataPinnedWithIndexListValue("a2");
        var bPin = new TestdataPinnedWithIndexListValue("bPin");
        var b1 = new TestdataPinnedWithIndexListValue("b1");
        var a = new TestdataPinnedWithIndexListEntity("A", aPin, a1, a2);
        a.setPinIndex(1);
        var b = new TestdataPinnedWithIndexListEntity("B", bPin, b1);
        b.setPinIndex(1);
        var solution = new TestdataPinnedWithIndexListSolution();
        solution.setEntityList(new ArrayList<>(List.of(a, b)));
        solution.setValueList(new ArrayList<>(List.of(aPin, a1, a2, bPin, b1)));
        var scoreDirector = buildScoreDirector(solutionDescriptor, solution, EnvironmentMode.PHASE_ASSERT);
        var startState = snapshot(solutionDescriptor, solution);

        Move<TestdataPinnedWithIndexListSolution> move = view -> {
            view.moveValueBetweenLists(listVariable, a, 1, b, 1);
            view.moveValueBetweenLists(listVariable, b, 2, a, 2);
            view.unassignValue(listVariable, b, 1);
        };
        assertThatCode(() -> scoreDirector.getMoveDirector().executeTemporary(move, score -> score))
                .doesNotThrowAnyException();
        assertThat(snapshot(solutionDescriptor, solution)).isEqualTo(startState);
    }

    // ************************************************************************
    // Ruin and recreate
    // ************************************************************************

    @Test
    void ruinRecreateBasicRestored() {
        var solutionDescriptor = TestdataSolution.buildSolutionDescriptor();
        var variableDescriptor = solutionDescriptor.findEntityDescriptorOrFail(TestdataEntity.class)
                .getGenuineVariableDescriptor("value");
        var v1 = new TestdataValue("v1");
        var v2 = new TestdataValue("v2");
        var v3 = new TestdataValue("v3");
        var e1 = new TestdataEntity("e1", v1);
        var e2 = new TestdataEntity("e2", v2);
        var e3 = new TestdataEntity("e3", v1);
        var solution = new TestdataSolution("s");
        solution.setValueList(List.of(v1, v2, v3));
        solution.setEntityList(List.of(e1, e2, e3));
        var originalValues = Map.of(e1, v1, e2, v2, e3, v1);
        // Steer the nested construction heuristic away from the original values.
        EasyScoreCalculator<TestdataSolution, SimpleScore> calculator = s -> SimpleScore.of(-(int) s.getEntityList().stream()
                .filter(e -> e.getValue() == null || e.getValue() == originalValues.get(e))
                .count());
        var scoreDirector = buildScoreDirector(solutionDescriptor, solution, EnvironmentMode.PHASE_ASSERT, calculator);
        var configPolicy = configPolicy(solutionDescriptor);
        var builder = RuinRecreateConstructionHeuristicPhaseBuilder.create(configPolicy,
                AbstractFromConfigFactory.getDefaultEntitySelectorConfigForEntity(configPolicy,
                        variableDescriptor.getEntityDescriptor()));
        var startState = snapshot(solutionDescriptor, solution);

        var move = new SelectorBasedRuinRecreateMove<>(variableDescriptor, builder, solverScope(scoreDirector),
                List.<Object> of(e1, e2), new LinkedHashSet<>(List.<Object> of(v1, v2)), 0L);
        var moveState = scoreDirector.getMoveDirector().executeTemporary(move,
                s -> snapshot(solutionDescriptor, s), false);

        assertThat(moveState).isNotEqualTo(startState);
        assertThat(snapshot(solutionDescriptor, solution)).isEqualTo(startState);
    }

    @Test
    void ruinRecreateListRestored() {
        var listVariableDescriptor = HISTORY_DESCRIPTOR.getListVariableDescriptor();
        var solution = historySolution(List.of(List.of("a0", "a1", "a2"), List.of("b0")), List.of());
        var a = solution.getEntityList().get(0);
        var b = solution.getEntityList().get(1);
        var a1 = solution.getValueList().get(1);
        var a2 = solution.getValueList().get(2);
        // Steer the nested construction heuristic to put the ruined values on B.
        EasyScoreCalculator<TestdataListSolutionWithShadowHistory, SimpleScore> calculator =
                s -> SimpleScore.of(-a.getValueList().size());
        var scoreDirector = buildScoreDirector(HISTORY_DESCRIPTOR, solution, EnvironmentMode.PHASE_ASSERT, calculator);
        var configPolicy = configPolicy(HISTORY_DESCRIPTOR);
        var entityPlacerConfig = DefaultConstructionHeuristicPhaseFactory
                .buildListVariableQueuedValuePlacerConfig(configPolicy, listVariableDescriptor);
        var builder = RuinRecreateConstructionHeuristicPhaseBuilder.create(configPolicy,
                new ConstructionHeuristicPhaseConfig().withEntityPlacerConfig(entityPlacerConfig));
        var startState = snapshot(HISTORY_DESCRIPTOR, solution);
        var startListState = listStateOf(scoreDirector, listVariableDescriptor, solution.getValueList());

        var move = new SelectorBasedListRuinRecreateMove<>(listVariableDescriptor, builder, solverScope(scoreDirector),
                List.<Object> of(a1, a2), new LinkedHashSet<>(List.<Object> of(a, b)), 0L);
        var moveState = scoreDirector.getMoveDirector().executeTemporary(move,
                s -> snapshot(HISTORY_DESCRIPTOR, s), false);

        assertThat(moveState).isNotEqualTo(startState);
        assertSoftly(softly -> {
            softly.assertThat(snapshot(HISTORY_DESCRIPTOR, solution)).isEqualTo(startState);
            softly.assertThat(listStateOf(scoreDirector, listVariableDescriptor, solution.getValueList()))
                    .isEqualTo(startListState);
        });
    }

    private static <Solution_> HeuristicConfigPolicy<Solution_> configPolicy(SolutionDescriptor<Solution_> descriptor) {
        return new HeuristicConfigPolicy.Builder<Solution_>()
                .withEnvironmentMode(EnvironmentMode.PHASE_ASSERT)
                .withSolutionDescriptor(descriptor)
                .withInitializingScoreTrend(InitializingScoreTrend.buildUniformTrend(InitializingScoreTrendLevel.ANY, 1))
                .build();
    }

    private static <Solution_> SolverScope<Solution_> solverScope(InnerScoreDirector<Solution_, SimpleScore> scoreDirector) {
        var solverScope = new SolverScope<Solution_>();
        solverScope.setScoreDirector(scoreDirector);
        return solverScope;
    }

    // ************************************************************************
    // Undo move and FULL_ASSERT
    // ************************************************************************

    @Test
    void undoMoveRestoresLaterAndCannotRebase() {
        var solution = historySolution(List.of(List.of("a0", "a1", "a2"), List.of("b0", "b1")), List.of("u"));
        var scoreDirector = buildScoreDirector(HISTORY_DESCRIPTOR, solution, EnvironmentMode.PHASE_ASSERT);
        var moveDirector = scoreDirector.getMoveDirector();
        var a = solution.getEntityList().get(0);
        var b = solution.getEntityList().get(1);
        var u = solution.getValueList().getLast();
        Move<TestdataListSolutionWithShadowHistory> move = view -> {
            view.moveValueBetweenLists(HISTORY_LIST_VARIABLE, a, 1, b, 0);
            view.moveValueBetweenLists(HISTORY_LIST_VARIABLE, b, 2, a, 0);
            view.assignValueAndAdd(HISTORY_LIST_VARIABLE, u, a, 1);
        };
        var listVariableDescriptor = HISTORY_DESCRIPTOR.getListVariableDescriptor();
        var startState = snapshot(HISTORY_DESCRIPTOR, solution);
        var startListState = listStateOf(scoreDirector, listVariableDescriptor, solution.getValueList());
        var startInitScore = scoreDirector.getWorkingInitScore();

        var undoMove = moveDirector.executeTemporaryProducingUndoMove(move, score -> {
        });
        assertThat(snapshot(HISTORY_DESCRIPTOR, solution)).isEqualTo(startState);

        moveDirector.execute(move);
        assertThat(snapshot(HISTORY_DESCRIPTOR, solution)).isNotEqualTo(startState);
        moveDirector.execute(undoMove);

        assertSoftly(softly -> {
            softly.assertThat(snapshot(HISTORY_DESCRIPTOR, solution)).isEqualTo(startState);
            softly.assertThat(listStateOf(scoreDirector, listVariableDescriptor, solution.getValueList()))
                    .isEqualTo(startListState);
            softly.assertThat(scoreDirector.getWorkingInitScore()).isEqualTo(startInitScore);
        });
        assertThatThrownBy(() -> undoMove.rebase(moveDirector));
    }

    @Test
    void undoMoveRestoresUnassignedCountWithPositionMap() {
        var solutionDescriptor = TestdataListSolution.buildSolutionDescriptor();
        var listVariableDescriptor = solutionDescriptor.getListVariableDescriptor();
        var variableMetaModel = solutionDescriptor.getMetaModel().genuineEntity(TestdataListEntity.class)
                .listVariable("valueList", TestdataListValue.class);
        var v0 = new TestdataListValue("v0");
        var v1 = new TestdataListValue("v1");
        var u = new TestdataListValue("u");
        var a = new TestdataListEntity("a", v0, v1);
        var solution = new TestdataListSolution();
        solution.setEntityList(List.of(a));
        solution.setValueList(List.of(v0, v1, u));
        var scoreDirector = buildScoreDirector(solutionDescriptor, solution, EnvironmentMode.PHASE_ASSERT);
        var moveDirector = scoreDirector.getMoveDirector();
        Move<TestdataListSolution> move = view -> {
            view.unassignValue(variableMetaModel, a, 0);
            view.assignValueAndAdd(variableMetaModel, u, a, 1);
        };
        var startState = snapshot(solutionDescriptor, solution);
        var startListState = listStateOf(scoreDirector, listVariableDescriptor, solution.getValueList());

        var undoMove = moveDirector.executeTemporaryProducingUndoMove(move, score -> {
        });
        moveDirector.execute(move);
        moveDirector.execute(undoMove);

        assertSoftly(softly -> {
            softly.assertThat(snapshot(solutionDescriptor, solution)).isEqualTo(startState);
            softly.assertThat(listStateOf(scoreDirector, listVariableDescriptor, solution.getValueList()))
                    .isEqualTo(startListState);
        });
    }

    @SuppressWarnings("unchecked")
    @Test
    void fullAssertDetectsCorruptedRestore() {
        var solution = historySolution(List.of(List.of("a0", "a1", "a2"), List.of("b0", "b1")), List.of());
        var scoreDirector = buildScoreDirector(HISTORY_DESCRIPTOR, solution, EnvironmentMode.FULL_ASSERT);
        var a = solution.getEntityList().get(0);
        var b = solution.getEntityList().get(1);
        Move<TestdataListSolutionWithShadowHistory> move = view -> {
            // Recorded first, so its undo runs last: it drops the last element of A without any event.
            Objects.requireNonNull(scoreDirector.getUndoJournal())
                    .recordSupplyChange((list, ignored, index) -> ((List<Object>) list).remove(index),
                            a.getValueList(), null, a.getValueList().size() - 1);
            view.moveValueBetweenLists(HISTORY_LIST_VARIABLE, a, 0, b, 0);
        };

        assertThatThrownBy(() -> scoreDirector.getMoveDirector().executeTemporary(move, score -> score))
                .isInstanceOf(UndoScoreCorruptionException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    void nonIntrusiveFullAssertSkipsRestoreCheck() {
        var solution = historySolution(List.of(List.of("a0", "a1", "a2"), List.of("b0", "b1")), List.of());
        var scoreDirector = buildScoreDirector(HISTORY_DESCRIPTOR, solution, EnvironmentMode.NON_INTRUSIVE_FULL_ASSERT);
        var a = solution.getEntityList().get(0);
        var b = solution.getEntityList().get(1);
        var restoreCount = new int[] { 0 };
        Move<TestdataListSolutionWithShadowHistory> move = view -> {
            // Recorded first, so its undo runs last; it corrupts the list state and counts the restores.
            Objects.requireNonNull(scoreDirector.getUndoJournal())
                    .recordSupplyChange((list, ignored, index) -> {
                        restoreCount[0]++;
                        ((List<Object>) list).remove(index);
                    }, a.getValueList(), null, a.getValueList().size() - 1);
            view.moveValueBetweenLists(HISTORY_LIST_VARIABLE, a, 0, b, 0);
        };

        assertThatCode(() -> scoreDirector.getMoveDirector().executeTemporary(move, score -> score))
                .doesNotThrowAnyException();
        assertThat(restoreCount[0]).isOne();
    }

    @Test
    void undoMoveFailsFastOnPendingShadowUpdate() {
        var solutionDescriptor = TestdataSingleCascadingSolution.buildSolutionDescriptor();
        var listVariableDescriptor = solutionDescriptor.getListVariableDescriptor();
        var listVariable = solutionDescriptor.getMetaModel()
                .genuineEntity(TestdataSingleCascadingEntity.class)
                .listVariable("valueList", TestdataSingleCascadingValue.class);
        var values = List.of(new TestdataSingleCascadingValue(1), new TestdataSingleCascadingValue(2),
                new TestdataSingleCascadingValue(3));
        var a = new TestdataSingleCascadingEntity("A", new ArrayList<>(values.subList(0, 2)));
        var b = new TestdataSingleCascadingEntity("B", new ArrayList<>(values.subList(2, 3)));
        var solution = new TestdataSingleCascadingSolution();
        solution.setValueList(values);
        solution.setEntityList(List.of(a, b));
        var scoreDirector = buildScoreDirector(solutionDescriptor, solution, EnvironmentMode.PHASE_ASSERT);
        var moveDirector = scoreDirector.getMoveDirector();
        Move<TestdataSingleCascadingSolution> move = view -> view.moveValueBetweenLists(listVariable, a, 0, b, 1);
        var undoMove = moveDirector.executeTemporaryProducingUndoMove(move, score -> {
        });
        moveDirector.execute(move);
        // An event without the shadow variable update that follows it.
        scoreDirector.beforeListVariableChanged(listVariableDescriptor, a, 0, 1);
        scoreDirector.afterListVariableChanged(listVariableDescriptor, a, 0, 1);

        assertThatThrownBy(() -> moveDirector.execute(undoMove))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shadow variable update is pending");
    }

    @Test
    void undoMoveInsideTemporaryRunIsRestoredWithListState() {
        var solution = historySolution(List.of(List.of("a0", "a1", "a2"), List.of("b0", "b1")), List.of("u"));
        var scoreDirector = buildScoreDirector(HISTORY_DESCRIPTOR, solution, EnvironmentMode.FULL_ASSERT);
        var moveDirector = scoreDirector.getMoveDirector();
        var a = solution.getEntityList().get(0);
        var b = solution.getEntityList().get(1);
        var u = solution.getValueList().getLast();
        Move<TestdataListSolutionWithShadowHistory> move = view -> {
            view.moveValueBetweenLists(HISTORY_LIST_VARIABLE, a, 1, b, 0);
            view.assignValueAndAdd(HISTORY_LIST_VARIABLE, u, a, 0);
            view.unassignValue(HISTORY_LIST_VARIABLE, b, 2);
        };
        var listVariableDescriptor = HISTORY_DESCRIPTOR.getListVariableDescriptor();
        var startState = snapshot(HISTORY_DESCRIPTOR, solution);
        var undoMove = moveDirector.executeTemporaryProducingUndoMove(move, score -> {
        });
        moveDirector.execute(move);
        var movedState = snapshot(HISTORY_DESCRIPTOR, solution);
        var movedListState = listStateOf(scoreDirector, listVariableDescriptor, solution.getValueList());
        var movedInitScore = scoreDirector.getWorkingInitScore();

        var undoneState = moveDirector.executeTemporary(undoMove, s -> snapshot(HISTORY_DESCRIPTOR, s), false);

        assertSoftly(softly -> {
            softly.assertThat(undoneState).isEqualTo(startState);
            softly.assertThat(snapshot(HISTORY_DESCRIPTOR, solution)).isEqualTo(movedState);
            softly.assertThat(listStateOf(scoreDirector, listVariableDescriptor, solution.getValueList()))
                    .isEqualTo(movedListState);
            softly.assertThat(scoreDirector.getWorkingInitScore()).isEqualTo(movedInitScore);
        });
    }

    @Test
    void undoMoveInsideTemporaryRunIsRestoredWithInverseSupply() {
        var solutionDescriptor = TestdataSolution.buildSolutionDescriptor();
        var variable = solutionDescriptor.getMetaModel()
                .genuineEntity(TestdataEntity.class)
                .basicVariable("value", TestdataValue.class);
        var v1 = new TestdataValue("v1");
        var v2 = new TestdataValue("v2");
        var e1 = new TestdataEntity("e1", v1);
        var e2 = new TestdataEntity("e2", v1);
        var e3 = new TestdataEntity("e3", v2);
        var solution = new TestdataSolution("s");
        solution.setValueList(List.of(v1, v2));
        solution.setEntityList(List.of(e1, e2, e3));
        var scoreDirector = buildScoreDirector(solutionDescriptor, solution, EnvironmentMode.FULL_ASSERT);
        var moveDirector = scoreDirector.getMoveDirector();
        var basicVariableState = scoreDirector.getBasicVariableState(
                solutionDescriptor.findEntityDescriptorOrFail(TestdataEntity.class).getGenuineVariableDescriptor("value"));
        Function<TestdataValue, Set<Object>> inverseOf =
                value -> new HashSet<>(basicVariableState.getInverseCollection(value));
        var startState = snapshot(solutionDescriptor, solution);
        Move<TestdataSolution> move = view -> {
            view.changeVariable(variable, e1, v2);
            view.changeVariable(variable, e3, v1);
        };
        var undoMove = moveDirector.executeTemporaryProducingUndoMove(move, score -> {
        });
        moveDirector.execute(move);
        var movedState = snapshot(solutionDescriptor, solution);
        var movedInverse = solution.getValueList().stream().map(inverseOf).toList();

        var undoneState = moveDirector.executeTemporary(undoMove, s -> snapshot(solutionDescriptor, s), false);

        assertSoftly(softly -> {
            softly.assertThat(undoneState).isEqualTo(startState);
            softly.assertThat(snapshot(solutionDescriptor, solution)).isEqualTo(movedState);
            softly.assertThat(solution.getValueList().stream().map(inverseOf).toList()).isEqualTo(movedInverse);
        });
    }

    // ************************************************************************
    // Event order and nesting
    // ************************************************************************

    @Test
    void undoOfAssignmentFiresElementEventsAroundListEvents() {
        var solutionDescriptor = TestdataListSolution.buildSolutionDescriptor();
        var variableMetaModel = solutionDescriptor.getMetaModel().genuineEntity(TestdataListEntity.class)
                .listVariable("valueList", TestdataListValue.class);
        var v0 = new TestdataListValue("v0");
        var u = new TestdataListValue("u");
        var a = new TestdataListEntity("a", v0);
        var solution = new TestdataListSolution();
        solution.setEntityList(List.of(a));
        solution.setValueList(List.of(v0, u));
        SolutionManager.updateShadowVariables(solution);
        var eventList = new ArrayList<String>();
        var scoreDirector = new IncrementalScoreDirectorFactory<TestdataListSolution, SimpleScore>(solutionDescriptor,
                () -> new EventRecordingCalculator(eventList), EnvironmentMode.PHASE_ASSERT)
                .createScoreDirectorBuilder(EnvironmentMode.PHASE_ASSERT)
                .build();
        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        Move<TestdataListSolution> move = view -> view.assignValueAndAdd(variableMetaModel, u, a, 1);

        scoreDirector.getMoveDirector().executeTemporary(move, score -> {
            eventList.clear();
            return score;
        });

        assertThat(eventList).containsExactly("beforeUnassigned(u)", "beforeListChanged(a, 1, 2)",
                "afterListChanged(a, 1, 1)", "afterUnassigned(u)");
    }

    @Test
    void nestedRestoreDoesNotGrowOuterJournal() {
        assertThat(ruinRecreateOuterJournalSize(3)).isEqualTo(ruinRecreateOuterJournalSize(30));
    }

    private static int ruinRecreateOuterJournalSize(int emptyEntityCount) {
        var listVariableDescriptor = HISTORY_DESCRIPTOR.getListVariableDescriptor();
        var valueCodeLists = new ArrayList<List<String>>();
        valueCodeLists.add(List.of("a0", "a1", "a2"));
        valueCodeLists.add(List.of("b0"));
        for (var i = 0; i < emptyEntityCount; i++) {
            valueCodeLists.add(List.of());
        }
        var solution = historySolution(valueCodeLists, List.of());
        var a = solution.getEntityList().get(0);
        var b = solution.getEntityList().get(1);
        var a1 = solution.getValueList().get(1);
        var a2 = solution.getValueList().get(2);
        // Only B is better than A, so every run puts the ruined values on B; more entities only mean more evaluations.
        EasyScoreCalculator<TestdataListSolutionWithShadowHistory, SimpleScore> calculator =
                s -> SimpleScore.of(-a.getValueList().size() + b.getValueList().size());
        var scoreDirector = buildScoreDirector(HISTORY_DESCRIPTOR, solution, EnvironmentMode.PHASE_ASSERT, calculator);
        var configPolicy = configPolicy(HISTORY_DESCRIPTOR);
        var entityPlacerConfig = DefaultConstructionHeuristicPhaseFactory
                .buildListVariableQueuedValuePlacerConfig(configPolicy, listVariableDescriptor);
        var builder = RuinRecreateConstructionHeuristicPhaseBuilder.create(configPolicy,
                new ConstructionHeuristicPhaseConfig().withEntityPlacerConfig(entityPlacerConfig));
        var startState = snapshot(HISTORY_DESCRIPTOR, solution);

        var move = new SelectorBasedListRuinRecreateMove<>(listVariableDescriptor, builder, solverScope(scoreDirector),
                List.<Object> of(a1, a2), new LinkedHashSet<>(List.<Object> of(a, b)), 0L);
        int journalSize = scoreDirector.getMoveDirector().executeTemporary(move,
                score -> Objects.requireNonNull(scoreDirector.getUndoJournal()).size());

        assertThat(b.getValueList()).hasSize(1);
        assertThat(snapshot(HISTORY_DESCRIPTOR, solution)).isEqualTo(startState);
        return journalSize;
    }

    private record EventRecordingCalculator(List<String> eventList)
            implements
                IncrementalScoreCalculator<TestdataListSolution, SimpleScore> {

        @Override
        public void resetWorkingSolution(TestdataListSolution workingSolution) {
            // Nothing to do.
        }

        @Override
        public void beforeVariableChanged(Object entity, String variableName) {
            // Shadow variable events are not checked.
        }

        @Override
        public void afterVariableChanged(Object entity, String variableName) {
            // Shadow variable events are not checked.
        }

        @Override
        public void beforeListVariableElementAssigned(String variableName, Object element) {
            eventList.add("beforeAssigned(%s)".formatted(element));
        }

        @Override
        public void afterListVariableElementAssigned(String variableName, Object element) {
            eventList.add("afterAssigned(%s)".formatted(element));
        }

        @Override
        public void beforeListVariableElementUnassigned(String variableName, Object element) {
            eventList.add("beforeUnassigned(%s)".formatted(element));
        }

        @Override
        public void afterListVariableElementUnassigned(String variableName, Object element) {
            eventList.add("afterUnassigned(%s)".formatted(element));
        }

        @Override
        public void beforeListVariableChanged(Object entity, String variableName, int fromIndex, int toIndex) {
            eventList.add("beforeListChanged(%s, %d, %d)".formatted(entity, fromIndex, toIndex));
        }

        @Override
        public void afterListVariableChanged(Object entity, String variableName, int fromIndex, int toIndex) {
            eventList.add("afterListChanged(%s, %d, %d)".formatted(entity, fromIndex, toIndex));
        }

        @Override
        public SimpleScore calculateScore() {
            return SimpleScore.ZERO;
        }

    }

    // ************************************************************************
    // Helpers
    // ************************************************************************

    private static <Solution_> InnerScoreDirector<Solution_, SimpleScore> buildScoreDirector(
            SolutionDescriptor<Solution_> solutionDescriptor, Solution_ solution, EnvironmentMode environmentMode) {
        return buildScoreDirector(solutionDescriptor, solution, environmentMode, s -> SimpleScore.ZERO);
    }

    private static <Solution_> InnerScoreDirector<Solution_, SimpleScore> buildScoreDirector(
            SolutionDescriptor<Solution_> solutionDescriptor, Solution_ solution, EnvironmentMode environmentMode,
            EasyScoreCalculator<Solution_, SimpleScore> calculator) {
        SolutionManager.updateShadowVariables(solution);
        var scoreDirectorFactory = new EasyScoreDirectorFactory<>(solutionDescriptor, calculator, environmentMode);
        var scoreDirector = scoreDirectorFactory.createScoreDirectorBuilder(environmentMode).build();
        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        return scoreDirector;
    }

    /**
     * Every genuine and shadow variable of every entity, keyed by entity and variable name.
     * Lists keep their order; other collections (inverse relation shadows) compare as sets.
     */
    private static <Solution_> Map<String, Object> snapshot(SolutionDescriptor<Solution_> solutionDescriptor,
            Solution_ solution) {
        var snapshot = new LinkedHashMap<String, Object>();
        var entityIndex = new int[] { 0 };
        solutionDescriptor.visitAllEntities(solution, entity -> {
            var entityDescriptor = solutionDescriptor.findEntityDescriptorOrFail(entity.getClass());
            var key = entityIndex[0]++ + ":" + entity;
            for (var variableDescriptor : entityDescriptor.getVariableDescriptorMap().values()) {
                var value = variableDescriptor.getValue(entity);
                Object copy;
                if (variableDescriptor instanceof ListVariableDescriptor<Solution_>) {
                    copy = new ArrayList<>((List<?>) value);
                } else if (value instanceof Collection<?> collection) {
                    copy = new HashSet<>(collection);
                } else {
                    copy = value;
                }
                snapshot.put(key + "." + variableDescriptor.getVariableName(), copy);
            }
        });
        return snapshot;
    }

    private static <Solution_> Map<Object, Object> listStateOf(InnerScoreDirector<Solution_, ?> scoreDirector,
            ListVariableDescriptor<Solution_> listVariableDescriptor, List<?> values) {
        ListVariableState<Solution_, Object, Object> state = scoreDirector.getListVariableState(listVariableDescriptor);
        var listState = new LinkedHashMap<Object, Object>();
        for (var value : values) {
            listState.put(value, Arrays.asList(state.getElementPosition(value), state.getIndexOrElse(value, -1),
                    state.getInverseSingleton(value), state.getPreviousElement(value), state.getNextElement(value)));
        }
        listState.put("unassignedCount", state.getUnassignedCount());
        return listState;
    }

}
