package com.sageb18.covered.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
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
    private LocalDateTime UnavailabilityStartTime;

    @Column(nullable = false)
    private LocalDateTime UnavailabilityEndTime;




}
