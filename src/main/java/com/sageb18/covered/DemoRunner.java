package com.sageb18.covered;

import ai.timefold.solver.core.api.solver.SolverManager;
import com.sageb18.covered.model.Employee;
import com.sageb18.covered.model.Role;
import com.sageb18.covered.solver.Schedule;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

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
        // test list of employees
        Employee sage = new Employee("sage@gmail.com", "hash", "Sage", "T", Role.EMPLOYEE);
        sage.setSkills(Set.of("BARISTA", "CLOSER"));
        Employee megu = new Employee("megu@yahoo.com", );

        // test list of shifts needing different skills

        // one empty assignment per shift

        // solve and wait for answer

        // print it
    }
}
