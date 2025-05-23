package ai.timefold.solver.core.impl.domain.lookup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import ai.timefold.solver.core.api.domain.lookup.LookUpStrategyType;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.testdomain.clone.lookup.TestdataObjectEnum;
import ai.timefold.solver.core.testdomain.clone.lookup.TestdataObjectIntegerId;
import ai.timefold.solver.core.testdomain.clone.lookup.TestdataObjectMultipleIds;
import ai.timefold.solver.core.testdomain.clone.lookup.TestdataObjectNoId;
import ai.timefold.solver.core.testdomain.clone.lookup.TestdataObjectPrimitiveIntId;

import org.junit.jupiter.api.Test;

class LookUpStrategyIdOrFailTest extends AbstractLookupTest {

    public LookUpStrategyIdOrFailTest() {
        super(LookUpStrategyType.PLANNING_ID_OR_FAIL_FAST);
    }

    @Test
    void addRemoveWithIntegerId() {
        TestdataObjectIntegerId object = new TestdataObjectIntegerId(0);
        lookUpManager.addWorkingObject(object);
        lookUpManager.removeWorkingObject(object);
        // The removed object cannot be looked up
        assertThat(lookUpManager.lookUpWorkingObjectOrReturnNull(object)).isNull();
    }

    @Test
    void addRemoveWithPrimitiveIntId() {
        TestdataObjectPrimitiveIntId object = new TestdataObjectPrimitiveIntId(0);
        lookUpManager.addWorkingObject(object);
        lookUpManager.removeWorkingObject(object);
        // The removed object cannot be looked up
        assertThat(lookUpManager.lookUpWorkingObjectOrReturnNull(object)).isNull();
    }

    @Test
    void addRemoveEnum() {
        TestdataObjectEnum object = TestdataObjectEnum.THIRD_VALUE;
        lookUpManager.addWorkingObject(object);
        lookUpManager.removeWorkingObject(object);
    }

    @Test
    void addWithNullId() {
        TestdataObjectIntegerId object = new TestdataObjectIntegerId(null);
        assertThatIllegalArgumentException().isThrownBy(() -> lookUpManager.addWorkingObject(object));
    }

    @Test
    void removeWithNullId() {
        TestdataObjectIntegerId object = new TestdataObjectIntegerId(null);
        assertThatIllegalArgumentException().isThrownBy(() -> lookUpManager.removeWorkingObject(object));
    }

    @Test
    void addWithoutId() {
        TestdataObjectNoId object = new TestdataObjectNoId();
        assertThatIllegalArgumentException().isThrownBy(() -> lookUpManager.addWorkingObject(object));
    }

    @Test
    void removeWithoutId() {
        TestdataObjectNoId object = new TestdataObjectNoId();
        assertThatIllegalArgumentException().isThrownBy(() -> lookUpManager.removeWorkingObject(object));
    }

    @Test
    void addSameIdTwice() {
        TestdataObjectIntegerId object = new TestdataObjectIntegerId(2);
        lookUpManager.addWorkingObject(object);
        assertThatIllegalStateException()
                .isThrownBy(() -> lookUpManager.addWorkingObject(new TestdataObjectIntegerId(2)))
                .withMessageContaining(" have the same planningId ")
                .withMessageContaining(object.toString());
    }

    @Test
    void removeWithoutAdding() {
        TestdataObjectIntegerId object = new TestdataObjectIntegerId(0);
        assertThatIllegalStateException()
                .isThrownBy(() -> lookUpManager.removeWorkingObject(object))
                .withMessageContaining("differ");
    }

    @Test
    void lookUpWithId() {
        TestdataObjectIntegerId object = new TestdataObjectIntegerId(1);
        lookUpManager.addWorkingObject(object);
        assertThat(lookUpManager.lookUpWorkingObject(new TestdataObjectIntegerId(1))).isSameAs(object);
    }

    @Test
    void lookUpWithoutId() {
        TestdataObjectNoId object = new TestdataObjectNoId();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> lookUpManager.lookUpWorkingObject(object))
                .withMessageContaining("does not have a @" + PlanningId.class.getSimpleName());
    }

    @Test
    void lookUpWithoutAdding() {
        TestdataObjectIntegerId object = new TestdataObjectIntegerId(0);
        assertThat(lookUpManager.lookUpWorkingObjectOrReturnNull(object)).isNull();
    }

    @Test
    void addWithTwoIds() {
        TestdataObjectMultipleIds object = new TestdataObjectMultipleIds();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> lookUpManager.addWorkingObject(object))
                .withMessageContaining("3 members")
                .withMessageContaining(PlanningId.class.getSimpleName());
    }

    @Test
    void removeWithTwoIds() {
        TestdataObjectMultipleIds object = new TestdataObjectMultipleIds();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> lookUpManager.removeWorkingObject(object))
                .withMessageContaining("3 members")
                .withMessageContaining(PlanningId.class.getSimpleName());
    }
}
