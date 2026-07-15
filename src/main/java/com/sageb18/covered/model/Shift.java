package com.sageb18.covered.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Shift {

    private UUID id;
    private LocalDateTime start;
    private LocalDateTime end;
    private String requiredSkill;

    public Shift() {
    }

    public Shift(UUID id, LocalDateTime start, LocalDateTime end, String requiredSkill) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.requiredSkill = requiredSkill;
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public String getRequiredSkill() {
        return requiredSkill;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Shift shift = (Shift) o;
        return Objects.equals(id, shift.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Shift{" + requiredSkill + " " + start + " to " + end + "}";
    }
}