package com.sageb18.covered;

import com.sageb18.covered.dto.AssignmentDto;
import com.sageb18.covered.dto.EmployeeDto;
import com.sageb18.covered.dto.ShiftDto;
import com.sageb18.covered.dto.SolveRequest;
import com.sageb18.covered.dto.SolveResponse;
import com.sageb18.covered.dto.ViolationDto;
import com.sageb18.covered.service.DemoScenario;
import com.sageb18.covered.service.SchedulingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Solves the demo scenario at startup and prints it.
 */
@Component
@Profile("demo")
public class DemoRunner implements CommandLineRunner {

    private final SchedulingService schedulingService;

    public DemoRunner(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @Override
    public void run(String... args) {
        SolveRequest request = DemoScenario.build();

        System.out.println("=== SOLVING SCHEDULE ===");
        SolveResponse response = schedulingService.solve(request);

        Map<UUID, String> employeeNames = request.employees().stream()
                .collect(Collectors.toMap(EmployeeDto::id, EmployeeDto::name));
        Map<UUID, String> shiftLabels = request.shifts().stream()
                .collect(Collectors.toMap(ShiftDto::id,
                        shift -> shift.requiredSkill() + " " + shift.dayOfWeek()
                                + " " + shift.start() + "-" + shift.end()));

        System.out.println("=== RESULTS ===");
        for (AssignmentDto assignment : response.assignments()) {
            System.out.println(shiftLabels.get(assignment.shiftId()) + " -> "
                    + employeeNames.getOrDefault(assignment.employeeId(), "UNASSIGNED"));
        }
        System.out.println("Score: " + response.hardScore() + "hard/" + response.softScore() + "soft"
                + (response.feasible() ? " (feasible)" : " (INFEASIBLE)"));
        for (ViolationDto violation : response.violations()) {
            System.out.println("  BROKEN  " + violation.constraint() + " x" + violation.count());
        }
        for (ViolationDto warning : response.warnings()) {
            System.out.println("  WARNING " + warning.constraint() + " x" + warning.count());
        }
    }
}
