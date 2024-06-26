package ai.timefold.solver.quarkus.benchmark.it;

import java.util.Arrays;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import ai.timefold.solver.benchmark.api.PlannerBenchmark;
import ai.timefold.solver.benchmark.api.PlannerBenchmarkFactory;
import ai.timefold.solver.quarkus.benchmark.it.domain.TestdataStringLengthShadowEntity;
import ai.timefold.solver.quarkus.benchmark.it.domain.TestdataStringLengthShadowSolution;

@Path("/timefold/test")
public class TimefoldBenchmarkTestResource {

    private final PlannerBenchmarkFactory benchmarkFactory;

    @Inject
    public TimefoldBenchmarkTestResource(PlannerBenchmarkFactory benchmarkFactory) {
        this.benchmarkFactory = benchmarkFactory;
    }

    @POST
    @Path("/benchmark")
    @Produces(MediaType.TEXT_PLAIN)
    public String benchmark() {
        TestdataStringLengthShadowSolution planningProblem = new TestdataStringLengthShadowSolution();
        planningProblem.setEntityList(Arrays.asList(
                new TestdataStringLengthShadowEntity(),
                new TestdataStringLengthShadowEntity()));
        planningProblem.setValueList(Arrays.asList("a", "bb", "ccc"));
        PlannerBenchmark benchmark = benchmarkFactory.buildPlannerBenchmark(planningProblem);
        return benchmark.benchmark().toPath().toAbsolutePath().toString();
    }
}
