package com.sageb18.covered.model;

import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int maxHours;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "employee_skills", joinColumns = @JoinColumn(name = "employee_id"))
    @Column(name = "skill", nullable = false)
    private Set<String> skills = new LinkedHashSet<>();

    public Employee() {
    }

    public Employee(UUID id, String name, int maxHours, Set<String> skills) {
        this.id = id;
        this.name = name;
        this.maxHours = maxHours;
        setSkills(skills);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxHours() {
        return maxHours;
    }

    public void setMaxHours(int maxHours) {
        this.maxHours = maxHours;
    }

    public Set<String> getSkills() {
        return skills;
    }

    /** never leaves skills null, the required-skill constraint calls contains() on it */
    public void setSkills(Set<String> skills) {
        this.skills = (skills == null) ? new LinkedHashSet<>() : new LinkedHashSet<>(skills);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return id != null && id.equals(employee.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return name;
    }
}
