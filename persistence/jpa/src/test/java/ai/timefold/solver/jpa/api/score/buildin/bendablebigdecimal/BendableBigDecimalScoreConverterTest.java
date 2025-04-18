package ai.timefold.solver.jpa.api.score.buildin.bendablebigdecimal;

import java.math.BigDecimal;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;

import ai.timefold.solver.core.api.score.buildin.bendablebigdecimal.BendableBigDecimalScore;
import ai.timefold.solver.jpa.impl.AbstractScoreJpaTest;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class BendableBigDecimalScoreConverterTest extends AbstractScoreJpaTest {

    @Test
    void persistAndMerge() {
        persistAndMerge(new BendableBigDecimalScoreConverterTestJpaEntity(BendableBigDecimalScore.zero(3, 2)), null,
                BendableBigDecimalScore.of(
                        new BigDecimal[] { new BigDecimal("10000.00001"), new BigDecimal("2000.00020"),
                                new BigDecimal("300.00300") },
                        new BigDecimal[] { new BigDecimal("40.04000"), new BigDecimal("5.50000") }));
    }

    @Entity
    static class BendableBigDecimalScoreConverterTestJpaEntity extends AbstractTestJpaEntity<BendableBigDecimalScore> {

        @Convert(converter = BendableBigDecimalScoreConverter.class)
        protected BendableBigDecimalScore score;

        BendableBigDecimalScoreConverterTestJpaEntity() {
        }

        public BendableBigDecimalScoreConverterTestJpaEntity(BendableBigDecimalScore score) {
            this.score = score;
        }

        @Override
        public BendableBigDecimalScore getScore() {
            return score;
        }

        @Override
        public void setScore(BendableBigDecimalScore score) {
            this.score = score;
        }
    }
}
