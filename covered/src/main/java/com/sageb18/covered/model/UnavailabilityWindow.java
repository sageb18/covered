package com.sageb18.covered.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "UnavailabilityWindow")
public class UnavailabilityWindow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private Instant unavailabilityStartTime;

    @Column(nullable = false)
    private Instant unavailabilityEndTime;

    public UnavailabilityWindow() {
    }

    public UnavailabilityWindow(Employee employee, Instant unavailabilityStartTime, Instant unavailabilityEndTime) {
        this.employee = employee;
        this.unavailabilityStartTime = unavailabilityStartTime;
        this.unavailabilityEndTime = unavailabilityEndTime;
    }

    public UUID getId() {
        return id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public Instant getUnavailabilityStartTime() {
        return unavailabilityStartTime;
    }

    public Instant getUnavailabilityEndTime() {
        return unavailabilityEndTime;
    }
}
