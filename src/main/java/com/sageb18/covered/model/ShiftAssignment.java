package com.sageb18.covered.model;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

import java.util.UUID;

@PlanningEntity
public class ShiftAssignment {

    @PlanningId
    private UUID id;

    private Shift shift;

    @PlanningVariable
    private Employee employee;

    public ShiftAssignment() {
    }

    public ShiftAssignment(UUID id, Shift shift) {
        this.id = id;
        this.shift = shift;
    }

    public UUID getId() {
        return id;
    }

    public Shift getShift() {
        return shift;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    @Override
    public String toString() {
        return shift + " -> " + (employee == null ? "UNASSIGNED" : employee);
    }
}