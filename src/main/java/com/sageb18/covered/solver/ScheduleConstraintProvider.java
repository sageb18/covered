package com.sageb18.covered.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import com.sageb18.covered.model.ShiftAssignment;
import com.sageb18.covered.model.UnavailabilityWindow;

import java.time.Duration;

public class ScheduleConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
                requiredSkill(factory),
                noOverlappingShifts(factory),
                maxHoursExceeded(factory),
                respectUnavailability(factory)
        };
    }

    // An employee must have the skill the shift requires.
    private Constraint requiredSkill(ConstraintFactory factory) {
        return factory.forEach(ShiftAssignment.class)
                .filter(assignment ->
                        assignment.getEmployee() != null &&
                        !assignment.getEmployee().getSkills().contains(assignment.getShift().getRequiredSkill()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Missing required skill");
    }

    // An employee cannot work two shifts that overlap in time.
    private Constraint noOverlappingShifts(ConstraintFactory factory) {
        return factory.forEachUniquePair(ShiftAssignment.class,
                        Joiners.equal(ShiftAssignment::getEmployee))
                .filter((a, b) ->
                        a.getEmployee() != null &&
                        a.getShift().getStart().isBefore(b.getShift().getEnd()) &&
                        b.getShift().getStart().isBefore(a.getShift().getEnd()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Overlapping shifts");
    }

    // An employee's total assigned hours must not exceed their maxHours.
    private Constraint maxHoursExceeded(ConstraintFactory factory) {
        return factory.forEach(ShiftAssignment.class)
                .filter(a -> a.getEmployee() != null)
                .groupBy(ShiftAssignment::getEmployee,
                        ConstraintCollectors.sum(a -> (int) Duration.between(
                                a.getShift().getStart(), a.getShift().getEnd()).toHours()))
                .filter((employee, totalHours) -> totalHours > employee.getMaxHours())
                .penalize(HardSoftScore.ONE_HARD,
                        (employee, totalHours) -> totalHours - employee.getMaxHours())
                .asConstraint("Max hours exceeded");
    }

    // An employee cannot be assigned a shift during a window they marked unavailable.
    private Constraint respectUnavailability(ConstraintFactory factory) {
        return factory.forEach(ShiftAssignment.class)
                .filter(a -> a.getEmployee() != null)
                .join(UnavailabilityWindow.class,
                        Joiners.equal(ShiftAssignment::getEmployee, UnavailabilityWindow::getEmployee))
                .filter((assignment, window) ->
                        assignment.getShift().getStart().isBefore(window.getUnavailabilityEndTime()) &&
                        window.getUnavailabilityStartTime().isBefore(assignment.getShift().getEnd()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Employee unavailable");
    }
}
