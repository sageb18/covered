package com.sageb18.covered.service;

/**
 * The names of the hard constraints, shared by the two places that need to agree on them:
 * ScheduleConstraintProvider (which declares them to the solver) and ScheduleExplainer
 * (which reports them to the UI).
 */
public final class ConstraintNames {

    public static final String MISSING_REQUIRED_SKILL = "Missing required skill";
    public static final String OVERLAPPING_SHIFTS = "Overlapping shifts";
    public static final String MAX_HOURS_EXCEEDED = "Max hours exceeded";
    public static final String EMPLOYEE_UNAVAILABLE = "Employee unavailable";

    private ConstraintNames() {
    }
}
