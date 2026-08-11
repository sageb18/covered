package com.sageb18.covered.service;

/**
 * The names of the constraints, shared by the two places that need to agree on them:
 * ScheduleConstraintProvider (which declares them to the solver) and ScheduleExplainer
 * (which reports them to the UI).
 */
public final class ConstraintNames {

    public static final String MISSING_REQUIRED_SKILL = "Missing required skill";
    public static final String OVERLAPPING_SHIFTS = "Overlapping shifts";
    public static final String MAX_HOURS_EXCEEDED = "Max hours exceeded";
    public static final String EMPLOYEE_UNAVAILABLE = "Employee unavailable";
    public static final String DAILY_HOURS_EXCEEDED = "Daily hours exceeded";

    /** The only soft constraint: reported as a warning, not as a broken rule. */
    public static final String DAILY_OVERTIME = "Daily overtime";

    private ConstraintNames() {
    }
}
