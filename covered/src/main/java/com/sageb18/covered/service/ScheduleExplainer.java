package com.sageb18.covered.service;

import com.sageb18.covered.dto.ViolationDto;
import com.sageb18.covered.model.Employee;
import com.sageb18.covered.model.Schedule;
import com.sageb18.covered.model.ShiftAssignment;
import com.sageb18.covered.model.UnavailabilityWindow;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Explains a solved schedule: which hard rules are broken, and how many times.
 *
 * Timefold would normally tell us this itself, but score analysis is a paid commercial feature on
 * Timefold 2.x versions.
 */
@Component
public class ScheduleExplainer {

    public List<ViolationDto> explain(Schedule solution) {
        // an unassigned shift cannot break any of these rules, and would NPE below
        List<ShiftAssignment> assigned = solution.getShiftAssignments().stream()
                .filter(assignment -> assignment.getEmployee() != null)
                .toList();

        Map<String, Integer> counts = new LinkedHashMap<>();
        countMissingSkills(assigned, counts);
        countOverlaps(assigned, counts);
        countMaxHoursBreaches(assigned, counts);
        countUnavailabilityBreaches(assigned, solution.getUnavailabilityWindows(), counts);

        List<ViolationDto> violations = new ArrayList<>(counts.size());
        counts.forEach((constraint, count) -> violations.add(new ViolationDto(constraint, count)));
        return List.copyOf(violations);
    }

    /** mirrors requiredSkill: forEach -> one match per assignment */
    private void countMissingSkills(List<ShiftAssignment> assigned, Map<String, Integer> counts) {
        for (ShiftAssignment assignment : assigned) {
            if (!assignment.getEmployee().getSkills().contains(assignment.getShift().getRequiredSkill())) {
                bump(counts, ConstraintNames.MISSING_REQUIRED_SKILL);
            }
        }
    }

    /** mirrors noOverlappingShifts: forEachUniquePair -> one match per unordered pair */
    private void countOverlaps(List<ShiftAssignment> assigned, Map<String, Integer> counts) {
        for (int i = 0; i < assigned.size(); i++) {
            for (int j = i + 1; j < assigned.size(); j++) {
                ShiftAssignment first = assigned.get(i);
                ShiftAssignment second = assigned.get(j);
                if (first.getEmployee().equals(second.getEmployee())
                        && first.getShift().overlaps(second.getShift())) {
                    bump(counts, ConstraintNames.OVERLAPPING_SHIFTS);
                }
            }
        }
    }

    /** mirrors maxHoursExceeded: groupBy employee -> one match per employee over their cap */
    private void countMaxHoursBreaches(List<ShiftAssignment> assigned, Map<String, Integer> counts) {
        Map<Employee, Integer> minutesByEmployee = new LinkedHashMap<>();
        for (ShiftAssignment assignment : assigned) {
            minutesByEmployee.merge(assignment.getEmployee(),
                    assignment.getShift().getDurationMinutes(), Integer::sum);
        }
        minutesByEmployee.forEach((employee, totalMinutes) -> {
            if (totalMinutes > employee.getMaxHours() * 60) {
                bump(counts, ConstraintNames.MAX_HOURS_EXCEEDED);
            }
        });
    }

    /** mirrors respectUnavailability: join -> one match per (assignment, window) pair */
    private void countUnavailabilityBreaches(List<ShiftAssignment> assigned,
                                             List<UnavailabilityWindow> windows,
                                             Map<String, Integer> counts) {
        if (windows == null) {
            return;
        }
        for (ShiftAssignment assignment : assigned) {
            for (UnavailabilityWindow window : windows) {
                if (window.getEmployee().equals(assignment.getEmployee())
                        && assignment.getShift().overlaps(window)) {
                    bump(counts, ConstraintNames.EMPLOYEE_UNAVAILABLE);
                }
            }
        }
    }
    private void bump(Map<String, Integer> counts, String constraint) {
        counts.merge(constraint, 1, Integer::sum);
    }
}
