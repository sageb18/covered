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

    /** A normal day's work. Anything past this is overtime. */
    static final int STANDARD_DAY_MINUTES = 8 * 60;

    /** How far past a standard day someone may be pushed before it stops being allowed at all. */
    static final int OVERTIME_GRACE_MINUTES = 2 * 60;

    /** The hard ceiling on a single day: a standard day plus the whole grace. */
    static final int MAX_DAY_MINUTES = STANDARD_DAY_MINUTES + OVERTIME_GRACE_MINUTES;

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
                requiredSkill(factory),
                noOverlappingShifts(factory),
                maxHoursExceeded(factory),
                respectUnavailability(factory),
                dailyHoursExceeded(factory),
                dailyOvertime(factory)
        };
    }

    // An employee must have the skill the shift requires.
    private Constraint requiredSkill(ConstraintFactory factory) {
        return factory.forEach(ShiftAssignment.class)
                .filter(assignment ->
                        !assignment.getEmployee().getSkills().contains(assignment.getShift().getRequiredSkill()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint(ConstraintNames.MISSING_REQUIRED_SKILL);
    }

    // An employee cannot work two shifts that overlap in time.
    private Constraint noOverlappingShifts(ConstraintFactory factory) {
        return factory.forEachUniquePair(ShiftAssignment.class,
                        Joiners.equal(ShiftAssignment::getEmployee))
                .filter((a, b) -> a.getShift().overlaps(b.getShift()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint(ConstraintNames.OVERLAPPING_SHIFTS);
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
                .asConstraint(ConstraintNames.MAX_HOURS_EXCEEDED);
    }

    // Nobody works more than MAX_DAY_MINUTES in a single day, ever.
    // Grouped by employee *and* day, so this is a per-day ceiling rather than a weekly one:
    // maxHoursExceeded above sums the whole schedule and would happily allow a 14-hour Monday
    // as long as the week's total stayed under the cap.
    private Constraint dailyHoursExceeded(ConstraintFactory factory) {
        return factory.forEach(ShiftAssignment.class)
                .groupBy(ShiftAssignment::getEmployee,
                        assignment -> assignment.getShift().getDayOfWeek(),
                        ConstraintCollectors.sum(a -> a.getShift().getDurationMinutes()))
                .filter((employee, day, dayMinutes) -> dayMinutes > MAX_DAY_MINUTES)
                .penalize(HardSoftScore.ONE_HARD,
                        (employee, day, dayMinutes) -> dayMinutes - MAX_DAY_MINUTES)
                .asConstraint(ConstraintNames.DAILY_HOURS_EXCEEDED);
    }

    // Overtime inside the grace band is allowed, but the solver should avoid it where it can.
    //
    // The penalty is the minutes spent in the grace band, so it climbs from 0 at a standard day
    // to OVERTIME_GRACE_MINUTES at the hard ceiling and then stops. Capping it matters: past the
    // ceiling dailyHoursExceeded takes over, and without the cap a longer day would keep adding
    // soft penalty on top of the hard one. Deliberately, both constraints fire on a day past the
    // ceiling -- such a day is genuinely both in overtime and over the limit, and ScheduleExplainer
    // mirrors that, so a 14-hour day reports one warning and one violation.
    private Constraint dailyOvertime(ConstraintFactory factory) {
        return factory.forEach(ShiftAssignment.class)
                .groupBy(ShiftAssignment::getEmployee,
                        assignment -> assignment.getShift().getDayOfWeek(),
                        ConstraintCollectors.sum(a -> a.getShift().getDurationMinutes()))
                .filter((employee, day, dayMinutes) -> dayMinutes > STANDARD_DAY_MINUTES)
                .penalize(HardSoftScore.ONE_SOFT,
                        (employee, day, dayMinutes) ->
                                Math.min(dayMinutes, MAX_DAY_MINUTES) - STANDARD_DAY_MINUTES)
                .asConstraint(ConstraintNames.DAILY_OVERTIME);
    }

    // An employee cannot be assigned a shift during a window they marked unavailable.
    private Constraint respectUnavailability(ConstraintFactory factory) {
        return factory.forEach(ShiftAssignment.class)
                .join(UnavailabilityWindow.class,
                        Joiners.equal(ShiftAssignment::getEmployee, UnavailabilityWindow::getEmployee))
                .filter((assignment, window) -> assignment.getShift().overlaps(window))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint(ConstraintNames.EMPLOYEE_UNAVAILABLE);
    }
}
