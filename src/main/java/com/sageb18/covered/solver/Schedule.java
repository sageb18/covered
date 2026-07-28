package com.sageb18.covered.solver;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import com.sageb18.covered.model.Employee;
import com.sageb18.covered.model.ShiftAssignment;

import java.util.List;

@PlanningSolution
public class Schedule {

    @ValueRangeProvider
    @ProblemFactCollectionProperty
    private List<Employee> employees;

    @PlanningEntityCollectionProperty
    private List<ShiftAssignment> shiftAssignments;

    @PlanningScore
    private HardSoftScore score;

    public Schedule() {
    }

    public Schedule(List<Employee> employees, List<ShiftAssignment> shiftAssignments) {
        this.employees = employees;
        this.shiftAssignments = shiftAssignments;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public List<ShiftAssignment> getShiftAssignments() {
        return shiftAssignments;
    }

    public HardSoftScore getScore() {
        return score;
    }

}
