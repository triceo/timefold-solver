package ai.timefold.solver.core.impl.testdata.domain.list.valuerange;

import java.util.List;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.domain.variable.PlanningListVariable;
import ai.timefold.solver.core.impl.testdata.domain.TestdataObject;

@PlanningEntity
public class TestdataListEntityProvidingEntity extends TestdataObject {

    @ValueRangeProvider(id = "valueRange")
    private final List<TestdataListEntityProvidingValue> valueRange;
    @PlanningListVariable(valueRangeProviderRefs = "valueRange")
    private List<TestdataListEntityProvidingValue> valueList;

    public TestdataListEntityProvidingEntity(List<TestdataListEntityProvidingValue> valueRange) {
        this.valueRange = valueRange;
    }

    public List<TestdataListEntityProvidingValue> getValueRange() {
        return valueRange;
    }

    public List<TestdataListEntityProvidingValue> getValueList() {
        return valueList;
    }

    public void setValueList(List<TestdataListEntityProvidingValue> valueList) {
        this.valueList = valueList;
    }
}
