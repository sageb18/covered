package com.sageb18.covered;

import ai.timefold.solver.core.api.solver.SolverJob;
import ai.timefold.solver.core.api.solver.SolverManager;
import com.sageb18.covered.model.Employee;
import com.sageb18.covered.model.Role;
import com.sageb18.covered.model.Shift;
import com.sageb18.covered.model.ShiftAssignment;
import com.sageb18.covered.model.UnavailabilityWindow;
import com.sageb18.covered.solver.Schedule;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class DemoRunner implements CommandLineRunner {

    private final SolverManager<Schedule, UUID> solverManager;

    public DemoRunner(SolverManager<Schedule, UUID> solverManager) {
        this.solverManager = solverManager;
    }

    @Override
    public void run(String... args) throws Exception {
        // Three employees with different skill sets
        Employee sage = new Employee("sage@gmail.com", "hash", "Taddeo", "Sage", 30, 20,
                Set.of("BARISTA", "CLOSER"), Role.EMPLOYEE);
        Employee megu = new Employee("megu@yahoo.com", "hash", "Megu", "Smith", 25, 30,
                Set.of("CASHIER", "OPENER"), Role.EMPLOYEE);
        Employee bon = new Employee("bon@gmail.com", "hash", "Bon", "Jones", 28, 25,
                Set.of("BARISTA", "CASHIER"), Role.EMPLOYEE);

        List<Employee> employees = List.of(sage, megu, bon);

        // Four shifts across two days requiring different skills
        Shift baristaMon = new Shift(UUID.randomUUID(),
                Instant.parse("2025-01-06T09:00:00Z"), Instant.parse("2025-01-06T17:00:00Z"), "BARISTA");
        Shift cashierMon = new Shift(UUID.randomUUID(),
                Instant.parse("2025-01-06T09:00:00Z"), Instant.parse("2025-01-06T17:00:00Z"), "CASHIER");
        Shift closerMon = new Shift(UUID.randomUUID(),
                Instant.parse("2025-01-06T17:00:00Z"), Instant.parse("2025-01-06T23:00:00Z"), "CLOSER");
        Shift baristaTue = new Shift(UUID.randomUUID(),
                Instant.parse("2025-01-07T09:00:00Z"), Instant.parse("2025-01-07T17:00:00Z"), "BARISTA");

        // One unassigned ShiftAssignment per shift — Timefold fills these in
        List<ShiftAssignment> assignments = List.of(
                new ShiftAssignment(UUID.randomUUID(), baristaMon),
                new ShiftAssignment(UUID.randomUUID(), cashierMon),
                new ShiftAssignment(UUID.randomUUID(), closerMon),
                new ShiftAssignment(UUID.randomUUID(), baristaTue)
        );

        Schedule problem = new Schedule(employees, List.of(), assignments);

        System.out.println("=== SOLVING SCHEDULE ===");
        SolverJob<Schedule, UUID> job = solverManager.solve(UUID.randomUUID(), problem);
        Schedule solution = job.getFinalBestSolution();

        System.out.println("=== RESULTS ===");
        for (ShiftAssignment a : solution.getShiftAssignments()) {
            System.out.println(a);
        }
        System.out.println("Score: " + solution.getScore());
    }
}
