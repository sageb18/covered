package com.sageb18.covered.dto;

import java.util.UUID;

/**
 * One solved pairing. Ids only -- the client already holds the names and shift details
 * it sent, so echoing them back would just be a second copy that can drift.
 *
 * <p>{@code employeeId} is null only if the solver left a shift unassigned, which the
 * current planning model does not allow.
 */
public record AssignmentDto(UUID shiftId, UUID employeeId) {
}
