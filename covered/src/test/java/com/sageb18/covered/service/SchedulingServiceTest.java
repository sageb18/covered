package com.sageb18.covered.service;

import com.sageb18.covered.dto.AssignmentDto;
import com.sageb18.covered.dto.EmployeeDto;
import com.sageb18.covered.dto.ShiftDto;
import com.sageb18.covered.dto.SolveRequest;
import com.sageb18.covered.dto.SolveResponse;
import com.sageb18.covered.dto.UnavailabilityDto;
import com.sageb18.covered.dto.ViolationDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * End-to-end through the real solver, but not over HTTP.
 *
 * <p>The termination override keeps these fast: every scenario here is small enough that
 * the construction heuristic settles it immediately, so there is no reason to sit through
 * the production five-second budget.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "timefold.solver.termination.spent-limit=1s",
        "timefold.solver.termination.best-score-limit=0hard/0soft"
})
class SchedulingServiceTest {

    @Autowired
    private SchedulingService schedulingService;

    private static final UUID SAGE = UUID.randomUUID();
    private static final UUID MEGU = UUID.randomUUID();
    private static final UUID BON = UUID.randomUUID();

    private static EmployeeDto employee(UUID id, String name, int maxHours, Set<String> skills,
                                        List<UnavailabilityDto> unavailability) {
        return new EmployeeDto(id, name, maxHours, skills, unavailability);
    }

    private static ShiftDto shift(UUID id, DayOfWeek day, int startHour, int endHour, String skill) {
        return new ShiftDto(id, day, LocalTime.of(startHour, 0), LocalTime.of(endHour, 0), skill);
    }

    /** shiftId -> employeeId, for readable assertions */
    private static Map<UUID, UUID> assignmentsByShift(SolveResponse response) {
        return response.assignments().stream()
                .collect(Collectors.toMap(AssignmentDto::shiftId, AssignmentDto::employeeId));
    }

    @Test
    @DisplayName("solves the demo scenario: unavailability pushes Bon off the Monday morning shift")
    void solvesFeasibleScenario() {
        UUID baristaMon = UUID.randomUUID();
        UUID cashierMon = UUID.randomUUID();
        UUID closerMon = UUID.randomUUID();

        SolveRequest request = new SolveRequest(
                List.of(
                        employee(SAGE, "Sage", 20, Set.of("BARISTA", "CLOSER"), List.of()),
                        employee(MEGU, "Megu", 30, Set.of("CASHIER"), List.of()),
                        employee(BON, "Bon", 25, Set.of("BARISTA", "CASHIER"),
                                List.of(new UnavailabilityDto(DayOfWeek.MONDAY,
                                        LocalTime.of(8, 0), LocalTime.of(12, 0))))),
                List.of(
                        shift(baristaMon, DayOfWeek.MONDAY, 9, 17, "BARISTA"),
                        shift(cashierMon, DayOfWeek.MONDAY, 9, 17, "CASHIER"),
                        shift(closerMon, DayOfWeek.MONDAY, 17, 23, "CLOSER")));

        SolveResponse response = schedulingService.solve(request);

        assertThat(response.feasible()).isTrue();
        assertThat(response.hardScore()).isZero();
        assertThat(response.violations()).isEmpty();
        assertThat(response.assignments()).hasSize(3);

        Map<UUID, UUID> assigned = assignmentsByShift(response);
        // Bon is unavailable Monday morning and Megu lacks BARISTA, so it must be Sage
        assertThat(assigned.get(baristaMon)).isEqualTo(SAGE);
        // only Sage holds CLOSER
        assertThat(assigned.get(closerMon)).isEqualTo(SAGE);
        assertThat(assigned.get(cashierMon)).isEqualTo(MEGU);
    }

    @Test
    @DisplayName("reports the constraint by name when nobody has the required skill")
    void reportsMissingSkill() {
        SolveRequest request = new SolveRequest(
                List.of(employee(SAGE, "Sage", 40, Set.of("BARISTA"), List.of())),
                List.of(shift(UUID.randomUUID(), DayOfWeek.MONDAY, 9, 17, "SOMMELIER")));

        SolveResponse response = schedulingService.solve(request);

        assertThat(response.feasible()).isFalse();
        assertThat(response.hardScore()).isNegative();
        assertThat(response.violations())
                .extracting(ViolationDto::constraint)
                .contains("Missing required skill");
        // even when infeasible we still hand back the best attempt, so the UI can show it
        assertThat(response.assignments()).hasSize(1);
        assertThat(response.assignments().getFirst().employeeId()).isEqualTo(SAGE);
    }

    @Test
    @DisplayName("counts each broken rule, so the UI can say 'x2'")
    void countsRepeatedViolations() {
        SolveRequest request = new SolveRequest(
                List.of(employee(SAGE, "Sage", 40, Set.of("BARISTA"), List.of())),
                List.of(
                        shift(UUID.randomUUID(), DayOfWeek.TUESDAY, 9, 17, "SOMMELIER"),
                        shift(UUID.randomUUID(), DayOfWeek.WEDNESDAY, 9, 17, "SOMMELIER")));

        SolveResponse response = schedulingService.solve(request);

        assertThat(response.violations())
                .filteredOn(v -> v.constraint().equals("Missing required skill"))
                .singleElement()
                .extracting(ViolationDto::count)
                .isEqualTo(2);
    }

    @Test
    @DisplayName("max-hours is enforced across shifts, proving employees group as one identity")
    void enforcesMaxHours() {
        // 3 x 8h = 24h of BARISTA work but Sage is capped at 10h, and she is the only
        // barista -- if the DTO->domain conversion leaked duplicate Employee objects,
        // groupBy would bucket these separately and this constraint would not fire.
        SolveRequest request = new SolveRequest(
                List.of(employee(SAGE, "Sage", 10, Set.of("BARISTA"), List.of())),
                List.of(
                        shift(UUID.randomUUID(), DayOfWeek.MONDAY, 9, 17, "BARISTA"),
                        shift(UUID.randomUUID(), DayOfWeek.TUESDAY, 9, 17, "BARISTA"),
                        shift(UUID.randomUUID(), DayOfWeek.WEDNESDAY, 9, 17, "BARISTA")));

        SolveResponse response = schedulingService.solve(request);

        assertThat(response.feasible()).isFalse();
        assertThat(response.violations())
                .extracting(ViolationDto::constraint)
                .contains("Max hours exceeded");
    }

    @Test
    @DisplayName("rejects duplicate employee ids instead of silently merging two people")
    void rejectsDuplicateEmployeeIds() {
        SolveRequest request = new SolveRequest(
                List.of(
                        employee(SAGE, "Sage", 20, Set.of("BARISTA"), List.of()),
                        employee(SAGE, "Impostor", 20, Set.of("BARISTA"), List.of())),
                List.of(shift(UUID.randomUUID(), DayOfWeek.MONDAY, 9, 17, "BARISTA")));

        assertThatThrownBy(() -> schedulingService.solve(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate employee id");
    }

    @Test
    @DisplayName("every shift in the request comes back in the response exactly once")
    void returnsOneAssignmentPerShift() {
        List<ShiftDto> shifts = List.of(
                shift(UUID.randomUUID(), DayOfWeek.MONDAY, 9, 12, "BARISTA"),
                shift(UUID.randomUUID(), DayOfWeek.MONDAY, 12, 15, "BARISTA"),
                shift(UUID.randomUUID(), DayOfWeek.MONDAY, 15, 18, "BARISTA"));

        SolveRequest request = new SolveRequest(
                List.of(employee(SAGE, "Sage", 40, Set.of("BARISTA"), List.of())),
                shifts);

        SolveResponse response = schedulingService.solve(request);

        assertThat(response.assignments())
                .extracting(AssignmentDto::shiftId)
                .containsExactlyInAnyOrderElementsOf(shifts.stream().map(ShiftDto::id).toList());
        assertThat(response.assignments())
                .extracting(AssignmentDto::employeeId)
                .allSatisfy(id -> assertThat(id).isNotNull());
    }

    @Test
    @DisplayName("an employee is never double-booked across overlapping shifts")
    void respectsOverlaps() {
        UUID early = UUID.randomUUID();
        UUID overlapping = UUID.randomUUID();

        SolveRequest request = new SolveRequest(
                List.of(
                        employee(SAGE, "Sage", 40, Set.of("BARISTA"), List.of()),
                        employee(MEGU, "Megu", 40, Set.of("BARISTA"), List.of())),
                List.of(
                        shift(early, DayOfWeek.MONDAY, 9, 17, "BARISTA"),
                        shift(overlapping, DayOfWeek.MONDAY, 13, 21, "BARISTA")));

        SolveResponse response = schedulingService.solve(request);

        assertThat(response.feasible()).isTrue();
        Map<UUID, UUID> assigned = assignmentsByShift(response);
        assertThat(assigned.get(early))
                .as("overlapping shifts must go to different people")
                .isNotEqualTo(assigned.get(overlapping));
    }

    @Test
    @DisplayName("touching shifts are not an overlap: 09-17 and 17-23 can be the same person")
    void treatsIntervalsAsHalfOpen() {
        UUID morning = UUID.randomUUID();
        UUID evening = UUID.randomUUID();

        SolveRequest request = new SolveRequest(
                List.of(employee(SAGE, "Sage", 40, Set.of("BARISTA"), List.of())),
                List.of(
                        shift(morning, DayOfWeek.MONDAY, 9, 17, "BARISTA"),
                        shift(evening, DayOfWeek.MONDAY, 17, 23, "BARISTA")));

        SolveResponse response = schedulingService.solve(request);

        assertThat(response.feasible()).isTrue();
        assertThat(response.violations()).isEmpty();
        Map<UUID, UUID> assigned = assignmentsByShift(response);
        assertThat(assigned.get(morning)).isEqualTo(SAGE);
        assertThat(assigned.get(evening)).isEqualTo(SAGE);
    }

    @Test
    @DisplayName("unavailability is matched to the right employee, not applied globally")
    void unavailabilityIsPerEmployee() {
        UUID mondayMorning = UUID.randomUUID();

        SolveRequest request = new SolveRequest(
                List.of(
                        employee(BON, "Bon", 40, Set.of("BARISTA"),
                                List.of(new UnavailabilityDto(DayOfWeek.MONDAY,
                                        LocalTime.of(8, 0), LocalTime.of(12, 0)))),
                        employee(MEGU, "Megu", 40, Set.of("BARISTA"), List.of())),
                List.of(shift(mondayMorning, DayOfWeek.MONDAY, 9, 11, "BARISTA")));

        SolveResponse response = schedulingService.solve(request);

        assertThat(response.feasible()).isTrue();
        assertThat(assignmentsByShift(response).get(mondayMorning))
                .as("Bon is unavailable, so Megu must take it")
                .isEqualTo(MEGU);
    }

    @Test
    @DisplayName("ids in the response are the ids the client sent, not regenerated ones")
    void echoesClientSuppliedIds() {
        UUID shiftId = UUID.randomUUID();

        SolveRequest request = new SolveRequest(
                List.of(employee(SAGE, "Sage", 40, Set.of("BARISTA"), List.of())),
                List.of(shift(shiftId, DayOfWeek.MONDAY, 9, 17, "BARISTA")));

        SolveResponse response = schedulingService.solve(request);

        AssignmentDto assignment = response.assignments().getFirst();
        assertThat(assignment.shiftId()).isEqualTo(shiftId);
        assertThat(assignment.employeeId()).isEqualTo(SAGE);
    }

    @Test
    @DisplayName("rejects duplicate shift ids")
    void rejectsDuplicateShiftIds() {
        UUID shiftId = UUID.randomUUID();
        SolveRequest request = new SolveRequest(
                List.of(employee(SAGE, "Sage", 40, Set.of("BARISTA"), List.of())),
                List.of(
                        shift(shiftId, DayOfWeek.MONDAY, 9, 17, "BARISTA"),
                        shift(shiftId, DayOfWeek.TUESDAY, 9, 17, "BARISTA")));

        assertThatThrownBy(() -> schedulingService.solve(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate shift id");
    }

    /**
     * The drift guard. ScheduleExplainer restates the rules that ScheduleConstraintProvider
     * gives the solver, so the two can fall out of step. They cannot disagree about whether
     * anything is broken at all: an empty violations list must mean a zero hard score, and a
     * negative hard score must come with at least one violation. If a constraint is ever
     * added or changed in only one of the two files, one of these scenarios fails.
     */
    @ParameterizedTest(name = "explainer agrees with solver: {0}")
    @MethodSource("everyScenario")
    @DisplayName("violations are empty exactly when the schedule is feasible")
    void explainerAgreesWithSolver(String name, SolveRequest request) {
        SolveResponse response = schedulingService.solve(request);

        assertThat(response.violations().isEmpty())
                .as("hardScore was %d but violations were %s", response.hardScore(), response.violations())
                .isEqualTo(response.hardScore() == 0);
        assertThat(response.feasible()).isEqualTo(response.hardScore() == 0);
        assertThat(response.violations())
                .allSatisfy(violation -> assertThat(violation.count()).isPositive());
    }

    static Stream<Arguments> everyScenario() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();

        return Stream.of(
                Arguments.of("feasible single shift", new SolveRequest(
                        List.of(employee(SAGE, "Sage", 40, Set.of("BARISTA"), List.of())),
                        List.of(shift(a, DayOfWeek.MONDAY, 9, 17, "BARISTA")))),

                Arguments.of("nobody has the skill", new SolveRequest(
                        List.of(employee(SAGE, "Sage", 40, Set.of("BARISTA"), List.of())),
                        List.of(shift(a, DayOfWeek.MONDAY, 9, 17, "SOMMELIER")))),

                Arguments.of("over max hours", new SolveRequest(
                        List.of(employee(SAGE, "Sage", 4, Set.of("BARISTA"), List.of())),
                        List.of(shift(a, DayOfWeek.MONDAY, 9, 17, "BARISTA")))),

                Arguments.of("forced overlap, one person two overlapping shifts", new SolveRequest(
                        List.of(employee(SAGE, "Sage", 40, Set.of("BARISTA"), List.of())),
                        List.of(shift(a, DayOfWeek.MONDAY, 9, 17, "BARISTA"),
                                shift(b, DayOfWeek.MONDAY, 13, 21, "BARISTA")))),

                Arguments.of("forced unavailability breach", new SolveRequest(
                        List.of(employee(BON, "Bon", 40, Set.of("BARISTA"),
                                List.of(new UnavailabilityDto(DayOfWeek.MONDAY,
                                        LocalTime.of(8, 0), LocalTime.of(18, 0))))),
                        List.of(shift(a, DayOfWeek.MONDAY, 9, 17, "BARISTA")))),

                Arguments.of("several rules broken at once", new SolveRequest(
                        List.of(employee(BON, "Bon", 2, Set.of("CASHIER"),
                                List.of(new UnavailabilityDto(DayOfWeek.MONDAY,
                                        LocalTime.of(0, 0), LocalTime.of(23, 59))))),
                        List.of(shift(a, DayOfWeek.MONDAY, 9, 17, "BARISTA"),
                                shift(b, DayOfWeek.MONDAY, 10, 18, "BARISTA")))),

                Arguments.of("comfortably feasible three-person week", new SolveRequest(
                        List.of(
                                employee(SAGE, "Sage", 40, Set.of("BARISTA", "CLOSER"), List.of()),
                                employee(MEGU, "Megu", 40, Set.of("CASHIER"), List.of()),
                                employee(BON, "Bon", 40, Set.of("BARISTA", "CASHIER"), List.of())),
                        List.of(
                                shift(a, DayOfWeek.MONDAY, 9, 17, "BARISTA"),
                                shift(b, DayOfWeek.MONDAY, 9, 17, "CASHIER"),
                                shift(c, DayOfWeek.MONDAY, 17, 23, "CLOSER")))));
    }
}
