package com.sageb18.covered.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import com.sageb18.covered.model.ShiftAssignment;

public class ScheduleConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
                requiredSkill(factory)
        };
    }

    // Constraint rule #1 for employees
    // Nobody should be assigned a shift they lack the skill for.
    private Constraint requiredSkill(ConstraintFactory factory) {
        return factory.forEach(ShiftAssignment.class)
                .filter(assignment ->
                        assignment.getEmployee() != null &&
                        !assignment.getEmployee().getSkills().contains(assignment.getShift().getRequiredSkill()))
                // ^^^ Lambda that runs once per assignment, returns a bool
                // "true" = rule is broken, "false" = rule not broken
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Missing required skill");
    }
}
