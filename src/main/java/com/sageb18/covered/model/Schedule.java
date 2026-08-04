package com.sageb18.covered.model;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.HardSoftScore;

import java.util.List;

@PlanningSolution
public class Schedule {

    @ValueRangeProvider
    @ProblemFactCollectionProperty
    private List<Employee> employees;

    @ProblemFactCollectionProperty
    private List<UnavailabilityWindow> unavailabilityWindows;

    @PlanningEntityCollectionProperty
    private List<ShiftAssignment> shiftAssignments;

    @PlanningScore
    private HardSoftScore score;

    public Schedule() {
    }

    public Schedule(List<Employee> employees, List<UnavailabilityWindow> unavailabilityWindows, List<ShiftAssignment> shiftAssignments) {
        this.employees = employees;
        this.unavailabilityWindows = unavailabilityWindows;
        this.shiftAssignments = shiftAssignments;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public List<UnavailabilityWindow> getUnavailabilityWindows() {
        return unavailabilityWindows;
    }

    public List<ShiftAssignment> getShiftAssignments() {
        return shiftAssignments;
    }

    public HardSoftScore getScore() {
        return score;
    }
}
