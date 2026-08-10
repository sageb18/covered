package com.sageb18.covered.dto;

import java.util.List;

public record SolveResponse(
        boolean feasible,
        long hardScore,
        long softScore,
        List<AssignmentDto> assignments,
        List<ViolationDto> violations) {

    public SolveResponse {
        assignments = (assignments == null) ? List.of() : List.copyOf(assignments);
        violations = (violations == null) ? List.of() : List.copyOf(violations);
    }
}
