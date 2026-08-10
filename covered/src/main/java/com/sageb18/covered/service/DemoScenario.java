package com.sageb18.covered.service;

import com.sageb18.covered.dto.EmployeeDto;
import com.sageb18.covered.dto.ShiftDto;
import com.sageb18.covered.dto.SolveRequest;
import com.sageb18.covered.dto.UnavailabilityDto;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A small solvable week, served by GET /api/demo-scenario so the UI has a "load example"
 * button. Ids are fixed rather than random so repeated loads are identical.
 */
public final class DemoScenario {

    private static final UUID SAGE = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    private static final UUID MEGU = UUID.fromString("00000000-0000-0000-0000-0000000000a2");
    private static final UUID BON = UUID.fromString("00000000-0000-0000-0000-0000000000a3");

    private static final UUID BARISTA_MON = UUID.fromString("00000000-0000-0000-0000-0000000000b1");
    private static final UUID CASHIER_MON = UUID.fromString("00000000-0000-0000-0000-0000000000b2");
    private static final UUID CLOSER_MON = UUID.fromString("00000000-0000-0000-0000-0000000000b3");
    private static final UUID BARISTA_TUE = UUID.fromString("00000000-0000-0000-0000-0000000000b4");

    private DemoScenario() {
    }

    public static SolveRequest build() {
        return new SolveRequest(
                List.of(
                        new EmployeeDto(SAGE, "Sage", 20, Set.of("BARISTA", "CLOSER"), List.of()),
                        new EmployeeDto(MEGU, "Megu", 30, Set.of("CASHIER", "OPENER"), List.of()),
                        // Bon can't work Monday mornings, which forces the Monday barista shift onto Sage
                        new EmployeeDto(BON, "Bon", 25, Set.of("BARISTA", "CASHIER"),
                                List.of(new UnavailabilityDto(DayOfWeek.MONDAY,
                                        LocalTime.of(8, 0), LocalTime.of(12, 0))))),
                List.of(
                        new ShiftDto(BARISTA_MON, DayOfWeek.MONDAY,
                                LocalTime.of(9, 0), LocalTime.of(17, 0), "BARISTA"),
                        new ShiftDto(CASHIER_MON, DayOfWeek.MONDAY,
                                LocalTime.of(9, 0), LocalTime.of(17, 0), "CASHIER"),
                        new ShiftDto(CLOSER_MON, DayOfWeek.MONDAY,
                                LocalTime.of(17, 0), LocalTime.of(23, 0), "CLOSER"),
                        new ShiftDto(BARISTA_TUE, DayOfWeek.TUESDAY,
                                LocalTime.of(9, 0), LocalTime.of(17, 0), "BARISTA")));
    }
}
