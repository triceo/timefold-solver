package ai.timefold.solver.jaxb.api.score.buildin.hardmediumsoft;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import ai.timefold.solver.jaxb.api.score.AbstractScoreJaxbAdapterTest;

import org.junit.jupiter.api.Test;

class HardMediumSoftScoreJaxbAdapterTest extends AbstractScoreJaxbAdapterTest {

    @Test
    void serializeAndDeserialize() {
        assertSerializeAndDeserialize(null, new TestHardMediumSoftScoreWrapper(null));

        var score = HardMediumSoftScore.of(1200, 30, 4);
        assertSerializeAndDeserialize(score, new TestHardMediumSoftScoreWrapper(score));
    }

    @XmlRootElement
    public static class TestHardMediumSoftScoreWrapper extends TestScoreWrapper<HardMediumSoftScore> {

        @XmlJavaTypeAdapter(HardMediumSoftScoreJaxbAdapter.class)
        private HardMediumSoftScore score;

        @SuppressWarnings("unused")
        private TestHardMediumSoftScoreWrapper() {
        }

        public TestHardMediumSoftScoreWrapper(HardMediumSoftScore score) {
            this.score = score;
        }

        @Override
        public HardMediumSoftScore getScore() {
            return score;
        }

    }

}
