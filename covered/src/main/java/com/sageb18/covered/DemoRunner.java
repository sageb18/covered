package com.sageb18.covered;

import ai.timefold.solver.core.api.solver.SolverJob;
import ai.timefold.solver.core.api.solver.SolverManager;
import com.sageb18.covered.model.Employee;
import com.sageb18.covered.model.Schedule;
import com.sageb18.covered.model.Shift;
import com.sageb18.covered.model.ShiftAssignment;
import com.sageb18.covered.model.UnavailabilityWindow;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class DemoRunner implements CommandLineRunner {

    private final SolverManager<Schedule> solverManager;

    public DemoRunner(SolverManager<Schedule> solverManager) {
        this.solverManager = solverManager;
    }

    @Override
    public void run(String... args) throws Exception {
        Employee sage = new Employee(UUID.randomUUID(), "Sage", 20, Set.of("BARISTA", "CLOSER"));
        Employee megu = new Employee(UUID.randomUUID(), "Megu", 30, Set.of("CASHIER", "OPENER"));
        Employee bon = new Employee(UUID.randomUUID(), "Bon", 25, Set.of("BARISTA", "CASHIER"));

        List<Employee> employees = List.of(sage, megu, bon);

        // Bon can't work Monday mornings -- forces the BARISTA Monday shift onto Sage.
        List<UnavailabilityWindow> unavailability = List.of(
                new UnavailabilityWindow(bon, DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0)));

        Shift baristaMon = new Shift(UUID.randomUUID(),
                DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0), "BARISTA");
        Shift cashierMon = new Shift(UUID.randomUUID(),
                DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0), "CASHIER");
        Shift closerMon = new Shift(UUID.randomUUID(),
                DayOfWeek.MONDAY, LocalTime.of(17, 0), LocalTime.of(23, 0), "CLOSER");
        Shift baristaTue = new Shift(UUID.randomUUID(),
                DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(17, 0), "BARISTA");

        List<ShiftAssignment> assignments = List.of(
                new ShiftAssignment(UUID.randomUUID(), baristaMon),
                new ShiftAssignment(UUID.randomUUID(), cashierMon),
                new ShiftAssignment(UUID.randomUUID(), closerMon),
                new ShiftAssignment(UUID.randomUUID(), baristaTue)
        );

        Schedule problem = new Schedule(employees, unavailability, assignments);

        System.out.println("=== SOLVING SCHEDULE ===");
        SolverJob<Schedule> job = solverManager.solve(UUID.randomUUID(), problem);
        Schedule solution = job.getFinalBestSolution();

        System.out.println("=== RESULTS ===");
        for (ShiftAssignment a : solution.getShiftAssignments()) {
            System.out.println(a);
        }
        System.out.println("Score: " + solution.getScore());
    }
}
