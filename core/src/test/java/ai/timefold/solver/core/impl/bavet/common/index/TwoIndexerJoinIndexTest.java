package ai.timefold.solver.core.impl.bavet.common.index;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import ai.timefold.solver.core.api.score.stream.Joiners;
import ai.timefold.solver.core.impl.bavet.bi.joiner.DefaultBiJoiner;
import ai.timefold.solver.core.impl.bavet.common.tuple.InTupleStorePositionTracker;
import ai.timefold.solver.core.impl.bavet.common.tuple.UniTuple;

import org.junit.jupiter.api.Test;

class TwoIndexerJoinIndexTest {

    // Left matches right when left.gender == right.gender and left.age < right.age.
    private final DefaultBiJoiner<TestPerson, TestPerson> equalGenderLessThanAge =
            (DefaultBiJoiner<TestPerson, TestPerson>) Joiners.equal(TestPerson::gender)
                    .and(Joiners.lessThan(TestPerson::age));

    @Test
    void defaultFactoryIsCommunity() {
        assertThat(AbstractIndexerFactory.of(equalGenderLessThanAge)).isInstanceOf(DefaultIndexerFactory.class);
    }

    @Test
    void reservesNoTupleStoreSlots() {
        var tracker = new CountingTracker();
        var joinIndex = new DefaultIndexerFactory<>(equalGenderLessThanAge).buildJoinIndex(tracker);
        assertThat(joinIndex).isInstanceOf(TwoIndexerJoinIndex.class);
        assertThat(tracker.reservationCount).isZero();
    }

    @Test
    void putQueryUpdateRemove() {
        JoinIndex<UniTuple<String>, UniTuple<String>> joinIndex =
                new DefaultIndexerFactory<>(equalGenderLessThanAge).buildJoinIndex(new CountingTracker());
        var left30 = UniTuple.of("left30", 0);
        var left30Entry = joinIndex.putLeft(left30, CompositeKey.ofMany("F", 30), left30);
        var left50 = UniTuple.of("left50", 0);
        var left50Entry = joinIndex.putLeft(left50, CompositeKey.ofMany("F", 50), left50);
        var right40 = UniTuple.of("right40", 0);
        var right40Key = CompositeKey.ofMany("F", 40);
        var right40Entry = joinIndex.putRight(right40, right40Key, right40);
        var otherGender = UniTuple.of("otherGender", 0);
        joinIndex.putRight(otherGender, CompositeKey.ofMany("M", 40), otherGender);

        assertThat(forEachLeft(joinIndex, right40, right40Key)).containsExactly(left30);
        assertThat(forEachRight(joinIndex, left30, CompositeKey.ofMany("F", 30))).containsExactly(right40);
        assertThat(joinIndex.sizeRight(left30, CompositeKey.ofMany("F", 30))).isEqualTo(1);
        assertThat(joinIndex.sizeRight(left50, CompositeKey.ofMany("F", 50))).isZero();

        // Moving right40 to 60 makes left50 match too.
        var right60Key = CompositeKey.ofMany("F", 60);
        right40Entry = joinIndex.updateRight(right40, right40Key, right60Key, right40Entry);
        assertThat(right40Entry.element()).isSameAs(right40);
        assertThat(forEachLeft(joinIndex, right40, right60Key)).containsExactlyInAnyOrder(left30, left50);
        assertThat(joinIndex.sizeRight(left50, CompositeKey.ofMany("F", 50))).isEqualTo(1);

        // Moving left30 to the other gender removes it from the matches.
        left30Entry = joinIndex.updateLeft(left30, CompositeKey.ofMany("F", 30), CompositeKey.ofMany("M", 30), left30Entry);
        assertThat(forEachLeft(joinIndex, right40, right60Key)).containsExactly(left50);
        assertThat(forEachRight(joinIndex, left30, CompositeKey.ofMany("M", 30))).containsExactly(otherGender);

        joinIndex.removeLeft(left50, CompositeKey.ofMany("F", 50), left50Entry);
        joinIndex.removeLeft(left30, CompositeKey.ofMany("M", 30), left30Entry);
        assertThat(forEachLeft(joinIndex, right40, right60Key)).isEmpty();
        joinIndex.removeRight(right40, right60Key, right40Entry);
        assertThat(joinIndex.sizeRight(left30, CompositeKey.ofMany("F", 0))).isZero();
    }

    private static List<UniTuple<String>> forEachLeft(JoinIndex<UniTuple<String>, UniTuple<String>> joinIndex,
            UniTuple<String> rightOwner, Object rightKey) {
        var result = new ArrayList<UniTuple<String>>();
        joinIndex.forEachLeft(rightOwner, rightKey, result::add);
        return result;
    }

    private static List<UniTuple<String>> forEachRight(JoinIndex<UniTuple<String>, UniTuple<String>> joinIndex,
            UniTuple<String> leftOwner, Object leftKey) {
        var result = new ArrayList<UniTuple<String>>();
        joinIndex.forEachRight(leftOwner, leftKey, result::add);
        return result;
    }

    private static final class CountingTracker implements InTupleStorePositionTracker {

        private int reservationCount = 0;

        @Override
        public int reserveNextLeft() {
            return reservationCount++;
        }

        @Override
        public int reserveNextRight() {
            return reservationCount++;
        }

    }

}
