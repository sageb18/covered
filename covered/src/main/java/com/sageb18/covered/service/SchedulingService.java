package com.sageb18.covered.service;

import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.core.api.solver.SolverManager;
import com.sageb18.covered.dto.AssignmentDto;
import com.sageb18.covered.dto.EmployeeDto;
import com.sageb18.covered.dto.ShiftDto;
import com.sageb18.covered.dto.SolveRequest;
import com.sageb18.covered.dto.SolveResponse;
import com.sageb18.covered.dto.UnavailabilityDto;
import com.sageb18.covered.dto.ViolationDto;
import com.sageb18.covered.model.Employee;
import com.sageb18.covered.model.Schedule;
import com.sageb18.covered.model.Shift;
import com.sageb18.covered.model.ShiftAssignment;
import com.sageb18.covered.model.UnavailabilityWindow;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * The boundary between the JSON contract and the Timefold solver.
 */
@Service
public class SchedulingService {

    private final SolverManager<Schedule> solverManager;
    private final ScheduleExplainer scheduleExplainer;

    public SchedulingService(SolverManager<Schedule> solverManager,
                             ScheduleExplainer scheduleExplainer) {
        this.solverManager = solverManager;
        this.scheduleExplainer = scheduleExplainer;
    }

    public SolveResponse solve(SolveRequest request) {
        Schedule problem = toSchedule(request);
        Schedule solution = solveBlocking(problem);
        return toResponse(solution);
    }

    // --- request -> solver -------------------------------------------------------

    /**
     * Builds the planning problem from the request.
     */
    private Schedule toSchedule(SolveRequest request) {
        Map<UUID, Employee> employeesById = new LinkedHashMap<>();
        for (EmployeeDto dto : request.employees()) {
            Employee employee = new Employee(dto.id(), dto.name(), dto.maxHours(), dto.skills());
            if (employeesById.putIfAbsent(dto.id(), employee) != null) {
                throw new IllegalArgumentException(
                        "Duplicate employee id " + dto.id() + " (each employee needs its own id)");
            }
        }

        List<UnavailabilityWindow> unavailabilityWindows = new ArrayList<>();
        for (EmployeeDto dto : request.employees()) {
            Employee employee = employeesById.get(dto.id());
            for (UnavailabilityDto window : dto.unavailability()) {
                unavailabilityWindows.add(new UnavailabilityWindow(
                        employee, window.dayOfWeek(), window.start(), window.end()));
            }
        }

        Set<UUID> seenShiftIds = new HashSet<>();
        List<ShiftAssignment> shiftAssignments = new ArrayList<>();
        for (ShiftDto dto : request.shifts()) {
            if (!seenShiftIds.add(dto.id())) {
                throw new IllegalArgumentException(
                        "Duplicate shift id " + dto.id() + " (each shift needs its own id)");
            }
            Shift shift = new Shift(dto.id(), dto.dayOfWeek(), dto.start(), dto.end(), dto.requiredSkill());
            // one assignment per shift, so the shift's id doubles as the @PlanningId
            shiftAssignments.add(new ShiftAssignment(dto.id(), shift));
        }

        return new Schedule(List.copyOf(employeesById.values()), unavailabilityWindows, shiftAssignments);
    }

    private Schedule solveBlocking(Schedule problem) {
        try {
            return solverManager.solve(UUID.randomUUID(), problem).getFinalBestSolution();
        } catch (InterruptedException e) {
            // restore the flag we just cleared, so callers up the stack still see the interrupt
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Solving was interrupted", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Solving failed: " + e.getCause().getMessage(), e.getCause());
        }
    }

    // --- solver -> response ------------------------------------------------------

    private SolveResponse toResponse(Schedule solution) {
        HardSoftScore score = solution.getScore();

        List<AssignmentDto> assignments = solution.getShiftAssignments().stream()
                .map(assignment -> new AssignmentDto(
                        assignment.getShift().getId(),
                        assignment.getEmployee() == null ? null : assignment.getEmployee().getId()))
                .toList();

        return new SolveResponse(
                score.hardScore() == 0,
                score.hardScore(),
                score.softScore(),
                assignments,
                scheduleExplainer.explain(solution));
    }
}
