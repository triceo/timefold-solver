
package ai.timefold.solver.quarkus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.quarkus.testdomain.dummy.DummyTestdataQuarkusEasyScoreCalculator;
import ai.timefold.solver.quarkus.testdomain.dummy.DummyTestdataQuarkusIncrementalScoreCalculator;
import ai.timefold.solver.quarkus.testdomain.dummy.DummyTestdataQuarkusShadowVariableEasyScoreCalculator;
import ai.timefold.solver.quarkus.testdomain.dummy.DummyTestdataQuarkusShadowVariableIncrementalScoreCalculator;
import ai.timefold.solver.quarkus.testdomain.normal.constraints.TestdataQuarkusConstraintProvider;
import ai.timefold.solver.quarkus.testdomain.normal.domain.TestdataQuarkusEntity;
import ai.timefold.solver.quarkus.testdomain.normal.domain.TestdataQuarkusSolution;
import ai.timefold.solver.quarkus.testdomain.shadowvariable.constraints.TestdataQuarkusShadowVariableConstraintProvider;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

class TimefoldProcessorMultipleSolversInvalidConstraintClassTest {

    // Empty classes
    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .overrideConfigKey("quarkus.timefold.solver.\"solver1\".environment-mode", "FULL_ASSERT")
            .overrideConfigKey("quarkus.timefold.solver.\"solver2\".environment-mode", "PHASE_ASSERT")
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(TestdataQuarkusEntity.class, TestdataQuarkusSolution.class))
            .assertException(t -> assertThat(t)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining(
                            "No classes found that implement EasyScoreCalculator, ConstraintProvider, or IncrementalScoreCalculator."));

    // Multiple classes - EasyScoreCalculator
    @RegisterExtension
    static final QuarkusUnitTest config2 = new QuarkusUnitTest()
            .overrideConfigKey("quarkus.timefold.solver.\"solver1\".environment-mode", "FULL_ASSERT")
            .overrideConfigKey("quarkus.timefold.solver.\"solver2\".environment-mode", "PHASE_ASSERT")
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(TestdataQuarkusEntity.class, TestdataQuarkusSolution.class,
                            DummyTestdataQuarkusEasyScoreCalculator.class)
                    .addClasses(DummyTestdataQuarkusShadowVariableEasyScoreCalculator.class))
            .assertException(t -> assertThat(t)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Some solver configs")
                    .hasMessageContaining("solver1")
                    .hasMessageContaining("solver2")
                    .hasMessageContaining("don't specify a EasyScoreCalculator score class, yet there are multiple available")
                    .hasMessageContaining(
                            "ai.timefold.solver.quarkus.testdomain.dummy.DummyTestdataQuarkusEasyScoreCalculator")
                    .hasMessageContaining(
                            "ai.timefold.solver.quarkus.testdomain.dummy.DummyTestdataQuarkusShadowVariableEasyScoreCalculator")
                    .hasMessageContaining("on the classpath."));
    // Multiple classes - EasyScoreCalculator with XML
    @RegisterExtension
    static final QuarkusUnitTest config3 = new QuarkusUnitTest()
            .overrideConfigKey("quarkus.timefold.solver.\"solver1\".environment-mode", "FULL_ASSERT")
            .overrideConfigKey("quarkus.timefold.solver.\"solver1\".solver-config-xml",
                    "ai/timefold/solver/quarkus/customSolverConfig.xml")
            .overrideConfigKey("quarkus.timefold.solver.\"solver2\".environment-mode", "PHASE_ASSERT")
            .overrideConfigKey("quarkus.timefold.solver.\"solver2\".solver-config-xml",
                    "ai/timefold/solver/quarkus/customSolverConfig.xml")
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(TestdataQuarkusEntity.class, TestdataQuarkusSolution.class)
                    .addClasses(DummyTestdataQuarkusEasyScoreCalculator.class,
                            DummyTestdataQuarkusShadowVariableEasyScoreCalculator.class)
                    .addAsResource("ai/timefold/solver/quarkus/customSolverConfig.xml"))
            .assertException(t -> assertThat(t)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Some solver configs")
                    .hasMessageContaining("solver1")
                    .hasMessageContaining("solver2")
                    .hasMessageContaining("don't specify a EasyScoreCalculator score class, yet there are multiple available")
                    .hasMessageContaining(
                            "ai.timefold.solver.quarkus.testdomain.dummy.DummyTestdataQuarkusEasyScoreCalculator")
                    .hasMessageContaining(
                            "ai.timefold.solver.quarkus.testdomain.dummy.DummyTestdataQuarkusShadowVariableEasyScoreCalculator")
                    .hasMessageContaining("on the classpath."));

    // Multiple classes - ConstraintProvider
    @RegisterExtension
    static final QuarkusUnitTest config4 = new QuarkusUnitTest()
            .overrideConfigKey("quarkus.timefold.solver.\"solver1\".environment-mode", "FULL_ASSERT")
            .overrideConfigKey("quarkus.timefold.solver.\"solver2\".environment-mode", "PHASE_ASSERT")
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(TestdataQuarkusEntity.class, TestdataQuarkusSolution.class,
                            TestdataQuarkusConstraintProvider.class)
                    .addClasses(TestdataQuarkusShadowVariableConstraintProvider.class))
            .assertException(t -> assertThat(t)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Some solver configs")
                    .hasMessageContaining("solver1")
                    .hasMessageContaining("solver2")
                    .hasMessageContaining("don't specify a ConstraintProvider score class, yet there are multiple available")
                    .hasMessageContaining(
                            "ai.timefold.solver.quarkus.testdomain.normal.constraints.TestdataQuarkusConstraintProvider")
                    .hasMessageContaining(
                            "ai.timefold.solver.quarkus.testdomain.shadowvariable.constraints.TestdataQuarkusShadowVariableConstraintProvider")
                    .hasMessageContaining("on the classpath."));

    // Multiple classes - ConstraintProvider with XML
    @RegisterExtension
    static final QuarkusUnitTest config5 = new QuarkusUnitTest()
            .overrideConfigKey("quarkus.timefold.solver.\"solver1\".environment-mode", "FULL_ASSERT")
            .overrideConfigKey("quarkus.timefold.solver.\"solver1\".solver-config-xml",
                    "ai/timefold/solver/quarkus/customSolverConfigWithoutScore.xml")
            .overrideConfigKey("quarkus.timefold.solver.\"solver2\".environment-mode", "PHASE_ASSERT")
            .overrideConfigKey("quarkus.timefold.solver.\"solver2\".solver-config-xml",
                    "ai/timefold/solver/quarkus/customSolverConfigWithoutScore.xml")
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(TestdataQuarkusEntity.class, TestdataQuarkusSolution.class)
                    .addClasses(TestdataQuarkusConstraintProvider.class, TestdataQuarkusShadowVariableConstraintProvider.class)
                    .addAsResource("ai/timefold/solver/quarkus/customSolverConfigWithoutScore.xml"))
            .assertException(t -> assertThat(t)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Some solver configs")
                    .hasMessageContaining("solver1")
                    .hasMessageContaining("solver2")
                    .hasMessageContaining("don't specify a ConstraintProvider score class, yet there are multiple available")
                    .hasMessageContaining(
                            "ai.timefold.solver.quarkus.testdomain.normal.constraints.TestdataQuarkusConstraintProvider")
                    .hasMessageContaining(
                            "ai.timefold.solver.quarkus.testdomain.shadowvariable.constraints.TestdataQuarkusShadowVariableConstraintProvider")
                    .hasMessageContaining("on the classpath."));

    // Multiple classes - IncrementalScoreCalculator
    @RegisterExtension
    static final QuarkusUnitTest config6 = new QuarkusUnitTest()
            .overrideConfigKey("quarkus.timefold.solver.\"solver1\".environment-mode", "FULL_ASSERT")
            .overrideConfigKey("quarkus.timefold.solver.\"solver2\".environment-mode", "PHASE_ASSERT")
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(TestdataQuarkusEntity.class, TestdataQuarkusSolution.class,
                            DummyTestdataQuarkusIncrementalScoreCalculator.class)
                    .addClasses(DummyTestdataQuarkusShadowVariableIncrementalScoreCalculator.class))
            .assertException(t -> assertThat(t)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Some solver configs")
                    .hasMessageContaining("solver1")
                    .hasMessageContaining("solver2")
                    .hasMessageContaining(
                            "don't specify a IncrementalScoreCalculator score class, yet there are multiple available")
                    .hasMessageContaining(
                            "ai.timefold.solver.quarkus.testdomain.dummy.DummyTestdataQuarkusIncrementalScoreCalculator")
                    .hasMessageContaining(
                            "ai.timefold.solver.quarkus.testdomain.dummy.DummyTestdataQuarkusShadowVariableIncrementalScoreCalculator")
                    .hasMessageContaining("on the classpath."));
    // Multiple classes - IncrementalScoreCalculator with XML
    @RegisterExtension
    static final QuarkusUnitTest config7 = new QuarkusUnitTest()
            .overrideConfigKey("quarkus.timefold.solver.\"solver1\".environment-mode", "FULL_ASSERT")
            .overrideConfigKey("quarkus.timefold.solver.\"solver1\".solver-config-xml",
                    "ai/timefold/solver/quarkus/customSolverConfigWithoutScore.xml")
            .overrideConfigKey("quarkus.timefold.solver.\"solver2\".environment-mode", "PHASE_ASSERT")
            .overrideConfigKey("quarkus.timefold.solver.\"solver2\".solver-config-xml",
                    "ai/timefold/solver/quarkus/customSolverConfigWithoutScore.xml")
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(TestdataQuarkusEntity.class, TestdataQuarkusSolution.class,
                            TestdataQuarkusConstraintProvider.class)
                    .addClasses(DummyTestdataQuarkusIncrementalScoreCalculator.class,
                            DummyTestdataQuarkusShadowVariableIncrementalScoreCalculator.class)
                    .addAsResource("ai/timefold/solver/quarkus/customSolverConfigWithoutScore.xml"))
            .assertException(t -> assertThat(t)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Some solver configs")
                    .hasMessageContaining("solver1")
                    .hasMessageContaining("solver2")
                    .hasMessageContaining(
                            "don't specify a IncrementalScoreCalculator score class, yet there are multiple available")
                    .hasMessageContaining(
                            "ai.timefold.solver.quarkus.testdomain.dummy.DummyTestdataQuarkusIncrementalScoreCalculator")
                    .hasMessageContaining(
                            "ai.timefold.solver.quarkus.testdomain.dummy.DummyTestdataQuarkusShadowVariableIncrementalScoreCalculator")
                    .hasMessageContaining("on the classpath."));

    // Unused classes - EasyScoreCalculator
    @RegisterExtension
    static final QuarkusUnitTest config8 = new QuarkusUnitTest()
            .overrideConfigKey("quarkus.timefold.solver.\"solver1\".solver-config-xml",
                    "ai/timefold/solver/quarkus/customSolverWithEasyScore.xml")
            .overrideConfigKey("quarkus.timefold.solver.\"solver2\".solver-config-xml",
                    "ai/timefold/solver/quarkus/customSolverWithEasyScore.xml")
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(TestdataQuarkusEntity.class, TestdataQuarkusSolution.class,
                            DummyTestdataQuarkusEasyScoreCalculator.class)
                    .addClasses(DummyTestdataQuarkusShadowVariableEasyScoreCalculator.class)
                    .addAsResource("ai/timefold/solver/quarkus/customSolverWithEasyScore.xml"))
            .assertException(t -> assertThat(t)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining(
                            "Unused classes ([ai.timefold.solver.quarkus.testdomain.dummy.DummyTestdataQuarkusShadowVariableEasyScoreCalculator]) that implements EasyScoreCalculator were found."));

    // Unused classes - ConstraintProvider
    @RegisterExtension
    static final QuarkusUnitTest config9 = new QuarkusUnitTest()
            .overrideConfigKey("quarkus.timefold.solver.\"solver1\".solver-config-xml",
                    "ai/timefold/solver/quarkus/customSolverQuarkusConfig.xml")
            .overrideConfigKey("quarkus.timefold.solver.\"solver2\".solver-config-xml",
                    "ai/timefold/solver/quarkus/customSolverQuarkusConfig.xml")
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(TestdataQuarkusEntity.class, TestdataQuarkusSolution.class,
                            TestdataQuarkusConstraintProvider.class)
                    .addClasses(TestdataQuarkusShadowVariableConstraintProvider.class)
                    .addAsResource("ai/timefold/solver/quarkus/customSolverQuarkusConfig.xml"))
            .assertException(t -> assertThat(t)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining(
                            "Unused classes ([ai.timefold.solver.quarkus.testdomain.shadowvariable.constraints.TestdataQuarkusShadowVariableConstraintProvider]) that implements ConstraintProvider were found."));

    // Unused classes - IncrementalScoreCalculator
    @RegisterExtension
    static final QuarkusUnitTest config10 = new QuarkusUnitTest()
            .overrideConfigKey("quarkus.timefold.solver.\"solver1\".solver-config-xml",
                    "ai/timefold/solver/quarkus/customSolverWithIncrementalScore.xml")
            .overrideConfigKey("quarkus.timefold.solver.\"solver2\".solver-config-xml",
                    "ai/timefold/solver/quarkus/customSolverWithIncrementalScore.xml")
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(TestdataQuarkusEntity.class, TestdataQuarkusSolution.class,
                            DummyTestdataQuarkusIncrementalScoreCalculator.class)
                    .addClasses(DummyTestdataQuarkusShadowVariableIncrementalScoreCalculator.class)
                    .addAsResource("ai/timefold/solver/quarkus/customSolverWithIncrementalScore.xml"))
            .assertException(t -> assertThat(t)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining(
                            "Unused classes ([ai.timefold.solver.quarkus.testdomain.dummy.DummyTestdataQuarkusShadowVariableIncrementalScoreCalculator]) that implements IncrementalScoreCalculator were found."));

    @Inject
    @Named("solver1")
    SolverManager<?, ?> solverManager1;

    @Inject
    @Named("solver2")
    SolverManager<?, ?> solverManager2;

    @Test
    void test() {
        fail("Should not call this method.");
    }
}
