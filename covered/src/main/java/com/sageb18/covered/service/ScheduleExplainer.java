package com.sageb18.covered.service;

import com.sageb18.covered.dto.ViolationDto;
import com.sageb18.covered.model.Employee;
import com.sageb18.covered.model.Schedule;
import com.sageb18.covered.model.ShiftAssignment;
import com.sageb18.covered.model.UnavailabilityWindow;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Explains a solved schedule: which rules are broken, and how many times.
 *
 * Timefold would normally tell us this itself, but score analysis is a paid commercial feature on
 * Timefold 2.x versions.
 */
@Component
public class ScheduleExplainer {

    /**
     * Hard breaches and soft ones, kept apart. Violations mean the schedule is unusable;
     * warnings mean it is usable but not ideal, so the UI can show them on a schedule that
     * was still found.
     */
    public record Explanation(List<ViolationDto> violations, List<ViolationDto> warnings) {
    }

    public Explanation explain(Schedule solution) {
        // an unassigned shift cannot break any of these rules, and would NPE below
        List<ShiftAssignment> assigned = solution.getShiftAssignments().stream()
                .filter(assignment -> assignment.getEmployee() != null)
                .toList();

        Map<String, Integer> hard = new LinkedHashMap<>();
        Map<String, Integer> soft = new LinkedHashMap<>();
        countMissingSkills(assigned, hard);
        countOverlaps(assigned, hard);
        countMaxHoursBreaches(assigned, hard);
        countUnavailabilityBreaches(assigned, solution.getUnavailabilityWindows(), hard);
        countDailyBreaches(assigned, hard, soft);

        return new Explanation(toViolations(hard), toViolations(soft));
    }

    private static List<ViolationDto> toViolations(Map<String, Integer> counts) {
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

    /**
     * mirrors dailyHoursExceeded and dailyOvertime: both group by (employee, day), so one match
     * each per employee-day over the relevant threshold. A day past the hard ceiling trips both,
     * exactly as it does in the solver.
     */
    private void countDailyBreaches(List<ShiftAssignment> assigned,
                                    Map<String, Integer> hard,
                                    Map<String, Integer> soft) {
        Map<Map.Entry<Employee, DayOfWeek>, Integer> minutesByEmployeeDay = new LinkedHashMap<>();
        for (ShiftAssignment assignment : assigned) {
            minutesByEmployeeDay.merge(
                    Map.entry(assignment.getEmployee(), assignment.getShift().getDayOfWeek()),
                    assignment.getShift().getDurationMinutes(), Integer::sum);
        }
        minutesByEmployeeDay.forEach((employeeDay, dayMinutes) -> {
            if (dayMinutes > ScheduleConstraintProvider.MAX_DAY_MINUTES) {
                bump(hard, ConstraintNames.DAILY_HOURS_EXCEEDED);
            }
            if (dayMinutes > ScheduleConstraintProvider.STANDARD_DAY_MINUTES) {
                bump(soft, ConstraintNames.DAILY_OVERTIME);
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
