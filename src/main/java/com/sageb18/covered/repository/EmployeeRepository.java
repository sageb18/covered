package com.sageb18.covered.repository;

import com.sageb18.covered.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository  extends JpaRepository<Employee, UUID> {
    Optional<Employee> findByEmail(String email);
    boolean existsByEmail(String email);
}
