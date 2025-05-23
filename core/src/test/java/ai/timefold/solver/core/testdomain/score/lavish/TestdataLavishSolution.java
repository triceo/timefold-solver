package ai.timefold.solver.core.testdomain.score.lavish;

import java.util.ArrayList;
import java.util.List;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.simple.SimpleScore;
import ai.timefold.solver.core.impl.domain.solution.descriptor.SolutionDescriptor;
import ai.timefold.solver.core.testdomain.TestdataObject;

@PlanningSolution
public class TestdataLavishSolution extends TestdataObject {

    public static SolutionDescriptor<TestdataLavishSolution> buildSolutionDescriptor() {
        return SolutionDescriptor.buildSolutionDescriptor(TestdataLavishSolution.class, TestdataLavishEntity.class);
    }

    public static TestdataLavishSolution generateSolution() {
        return generateSolution(2, 5, 3, 7);
    }

    public static TestdataLavishSolution generateSolution(int valueListSize, int entityListSize) {
        return generateSolution(2, valueListSize, 3, entityListSize);
    }

    public static TestdataLavishSolution generateSolution(int valueGroupListSize, int valueListSize,
            int entityGroupListSize, int entityListSize) {
        TestdataLavishSolution solution = new TestdataLavishSolution("Generated Solution 0");
        List<TestdataLavishValueGroup> valueGroupList = new ArrayList<>(valueGroupListSize);
        for (int i = 0; i < valueGroupListSize; i++) {
            TestdataLavishValueGroup valueGroup = new TestdataLavishValueGroup("Generated ValueGroup " + i);
            valueGroupList.add(valueGroup);
        }
        solution.setValueGroupList(valueGroupList);
        List<TestdataLavishValue> valueList = new ArrayList<>(valueListSize);
        for (int i = 0; i < valueListSize; i++) {
            TestdataLavishValueGroup valueGroup = valueGroupList.get(i % valueGroupListSize);
            TestdataLavishValue value = new TestdataLavishValue("Generated Value " + i, valueGroup);
            valueList.add(value);
        }
        solution.setValueList(valueList);
        solution.setExtraList(new ArrayList<>());
        List<TestdataLavishEntityGroup> entityGroupList = new ArrayList<>(entityGroupListSize);
        for (int i = 0; i < entityGroupListSize; i++) {
            TestdataLavishEntityGroup entityGroup = new TestdataLavishEntityGroup("Generated EntityGroup " + i);
            entityGroupList.add(entityGroup);
        }
        solution.setEntityGroupList(entityGroupList);
        List<TestdataLavishEntity> entityList = new ArrayList<>(entityListSize);
        for (int i = 0; i < entityListSize; i++) {
            TestdataLavishEntityGroup entityGroup = entityGroupList.get(i % entityGroupListSize);
            TestdataLavishValue value = valueList.get(i % valueListSize);
            TestdataLavishEntity entity = new TestdataLavishEntity("Generated Entity " + i, entityGroup, value);
            entity.setLongProperty(Math.round(Math.random() * 1_000_000L));
            entityList.add(entity);
        }
        solution.setEntityList(entityList);
        return solution;
    }

    @ProblemFactCollectionProperty
    private List<TestdataLavishValueGroup> valueGroupList;
    @ValueRangeProvider(id = "valueRange")
    @ProblemFactCollectionProperty
    private List<TestdataLavishValue> valueList;
    @ProblemFactCollectionProperty
    private List<TestdataLavishExtra> extraList;
    @ProblemFactCollectionProperty
    private List<TestdataLavishEntityGroup> entityGroupList;
    @PlanningEntityCollectionProperty
    private List<TestdataLavishEntity> entityList;

    @PlanningScore
    private SimpleScore score;

    public TestdataLavishSolution() {
    }

    public TestdataLavishSolution(String code) {
        super(code);
    }

    public TestdataLavishValueGroup getFirstValueGroup() {
        return valueGroupList.get(0);
    }

    public TestdataLavishValue getFirstValue() {
        return valueList.get(0);
    }

    public TestdataLavishEntityGroup getFirstEntityGroup() {
        return entityGroupList.get(0);
    }

    public TestdataLavishEntity getFirstEntity() {
        return entityList.get(0);
    }

    // ************************************************************************
    // Getter/setters
    // ************************************************************************

    public List<TestdataLavishValueGroup> getValueGroupList() {
        return valueGroupList;
    }

    public void setValueGroupList(List<TestdataLavishValueGroup> valueGroupList) {
        this.valueGroupList = valueGroupList;
    }

    public List<TestdataLavishValue> getValueList() {
        return valueList;
    }

    public void setValueList(List<TestdataLavishValue> valueList) {
        this.valueList = valueList;
    }

    public List<TestdataLavishExtra> getExtraList() {
        return extraList;
    }

    public void setExtraList(List<TestdataLavishExtra> extraList) {
        this.extraList = extraList;
    }

    public List<TestdataLavishEntityGroup> getEntityGroupList() {
        return entityGroupList;
    }

    public void setEntityGroupList(List<TestdataLavishEntityGroup> entityGroupList) {
        this.entityGroupList = entityGroupList;
    }

    public List<TestdataLavishEntity> getEntityList() {
        return entityList;
    }

    public void setEntityList(List<TestdataLavishEntity> entityList) {
        this.entityList = entityList;
    }

    public SimpleScore getScore() {
        return score;
    }

    public void setScore(SimpleScore score) {
        this.score = score;
    }

}
