package com.sageb18.covered.service;

import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import com.sageb18.covered.model.ShiftAssignment;
import com.sageb18.covered.model.UnavailabilityWindow;

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
                        !assignment.getEmployee().getSkills().contains(assignment.getShift().getRequiredSkill()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Missing required skill");
    }

    // An employee cannot work two shifts that overlap in time.
    private Constraint noOverlappingShifts(ConstraintFactory factory) {
        return factory.forEachUniquePair(ShiftAssignment.class,
                        Joiners.equal(ShiftAssignment::getEmployee))
                .filter((a, b) -> a.getShift().overlaps(b.getShift()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Overlapping shifts");
    }

    // An employee's total assigned hours must not exceed their maxHours.
    // Summed in minutes so half-hour shifts aren't truncated away.
    private Constraint maxHoursExceeded(ConstraintFactory factory) {
        return factory.forEach(ShiftAssignment.class)
                .groupBy(ShiftAssignment::getEmployee,
                        ConstraintCollectors.sum(a -> a.getShift().getDurationMinutes()))
                .filter((employee, totalMinutes) -> totalMinutes > employee.getMaxHours() * 60)
                .penalize(HardSoftScore.ONE_HARD,
                        (employee, totalMinutes) -> totalMinutes - employee.getMaxHours() * 60)
                .asConstraint("Max hours exceeded");
    }

    // An employee cannot be assigned a shift during a window they marked unavailable.
    private Constraint respectUnavailability(ConstraintFactory factory) {
        return factory.forEach(ShiftAssignment.class)
                .join(UnavailabilityWindow.class,
                        Joiners.equal(ShiftAssignment::getEmployee, UnavailabilityWindow::getEmployee))
                .filter((assignment, window) -> assignment.getShift().overlaps(window))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Employee unavailable");
    }
}
