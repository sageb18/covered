package com.sageb18.covered.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "ShiftAssignment")
public class ShiftAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
}
