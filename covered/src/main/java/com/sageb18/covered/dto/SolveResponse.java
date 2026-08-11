package com.sageb18.covered.dto;

import java.util.List;

/**
 * @param violations hard rules the schedule breaks: non-empty exactly when it is infeasible
 * @param warnings   soft rules it bends. The schedule is still usable, so these can appear
 *                   alongside {feasible: true} and an empty violations list.
 */
public record SolveResponse(
        boolean feasible,
        long hardScore,
        long softScore,
        List<AssignmentDto> assignments,
        List<ViolationDto> violations,
        List<ViolationDto> warnings) {

    public SolveResponse {
        assignments = (assignments == null) ? List.of() : List.copyOf(assignments);
        violations = (violations == null) ? List.of() : List.copyOf(violations);
        warnings = (warnings == null) ? List.of() : List.copyOf(warnings);
    }

    /** A response with nothing to warn about. */
    public SolveResponse(boolean feasible, long hardScore, long softScore,
                         List<AssignmentDto> assignments, List<ViolationDto> violations) {
        this(feasible, hardScore, softScore, assignments, violations, List.of());
    }
}
