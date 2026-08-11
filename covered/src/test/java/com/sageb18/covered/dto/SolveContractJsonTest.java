package com.sageb18.covered.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Locks down the /solve JSON contract.
 *
 * <p>@JsonTest boots only Jackson -- not the database or the solver -- using the same
 * ObjectMapper configuration the real endpoints use. That matters: plain Jackson writes
 * a LocalTime as [9,0], and it is Spring Boot's configuration that turns it into "09:00".
 * Testing with a hand-built mapper would prove nothing about the running app.
 */
@JsonTest
class SolveContractJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("request JSON parses day-of-week names and HH:mm times with no custom converter")
    void deserialisesRequest() throws Exception {
        String json = """
                {
                  "employees": [{
                    "id": "11111111-1111-1111-1111-111111111111",
                    "name": "Sage",
                    "maxHours": 20,
                    "skills": ["BARISTA", "CLOSER"],
                    "unavailability": [
                      {"dayOfWeek": "MONDAY", "start": "08:00", "end": "12:00"}
                    ]
                  }],
                  "shifts": [{
                    "id": "22222222-2222-2222-2222-222222222222",
                    "dayOfWeek": "MONDAY",
                    "start": "09:00",
                    "end": "17:00",
                    "requiredSkill": "BARISTA"
                  }]
                }
                """;

        SolveRequest request = objectMapper.readValue(json, SolveRequest.class);

        EmployeeDto employee = request.employees().getFirst();
        assertThat(employee.id()).isEqualTo(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        assertThat(employee.name()).isEqualTo("Sage");
        assertThat(employee.maxHours()).isEqualTo(20);
        assertThat(employee.skills()).containsExactlyInAnyOrder("BARISTA", "CLOSER");
        assertThat(employee.unavailability()).singleElement().satisfies(window -> {
            assertThat(window.dayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
            assertThat(window.start()).isEqualTo(LocalTime.of(8, 0));
            assertThat(window.end()).isEqualTo(LocalTime.of(12, 0));
        });

        ShiftDto shift = request.shifts().getFirst();
        assertThat(shift.dayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(shift.start()).isEqualTo(LocalTime.of(9, 0));
        assertThat(shift.end()).isEqualTo(LocalTime.of(17, 0));
        assertThat(shift.requiredSkill()).isEqualTo("BARISTA");
    }

    @Test
    @DisplayName("omitted collections become empty, never null")
    void normalisesMissingCollections() throws Exception {
        String json = """
                {
                  "employees": [{
                    "id": "11111111-1111-1111-1111-111111111111",
                    "name": "Sage",
                    "maxHours": 20
                  }],
                  "shifts": []
                }
                """;

        SolveRequest request = objectMapper.readValue(json, SolveRequest.class);

        assertThat(request.employees().getFirst().skills()).isEmpty();
        assertThat(request.employees().getFirst().unavailability()).isEmpty();
    }

    @Test
    @DisplayName("a shift serialises back as HH:mm and leaks no validation helper field")
    void serialisesShiftCleanly() throws Exception {
        ShiftDto shift = new ShiftDto(
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0), "BARISTA");

        String json = objectMapper.writeValueAsString(shift);

        assertThat(json).contains("\"start\":\"09:00\"", "\"end\":\"17:00\"", "\"dayOfWeek\":\"MONDAY\"");
        assertThat(json).doesNotContain("endAfterStart");

        // what we write must be something we can read back -- otherwise /api/demo-scenario
        // would emit a payload the client cannot post straight back to /api/solve
        assertThat(objectMapper.readValue(json, ShiftDto.class)).isEqualTo(shift);
    }

    @Test
    @DisplayName("response serialises to exactly the agreed field names")
    void serialisesResponse() throws Exception {
        UUID shiftId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID employeeId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        SolveResponse response = new SolveResponse(false, -2, 0,
                List.of(new AssignmentDto(shiftId, employeeId)),
                List.of(new ViolationDto("Missing required skill", 2)));

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"feasible\":false", "\"hardScore\":-2", "\"softScore\":0");
        assertThat(json).contains("\"shiftId\":\"" + shiftId + "\"", "\"employeeId\":\"" + employeeId + "\"");
        assertThat(json).contains("\"constraint\":\"Missing required skill\"", "\"count\":2");
        // present even when there is nothing to warn about, so the client can read it unguarded
        assertThat(json).contains("\"warnings\":[]");
    }

    @Test
    @DisplayName("warnings serialise alongside a feasible schedule, separately from violations")
    void serialisesWarnings() throws Exception {
        SolveResponse response = new SolveResponse(true, 0, -60,
                List.of(),
                List.of(),
                List.of(new ViolationDto("Daily overtime", 1)));

        String json = objectMapper.writeValueAsString(response);

        // a soft breach must not make the schedule look broken
        assertThat(json).contains("\"feasible\":true", "\"hardScore\":0", "\"softScore\":-60");
        assertThat(json).contains("\"violations\":[]");
        assertThat(json).contains("\"warnings\":[{\"constraint\":\"Daily overtime\",\"count\":1}]");
    }
}
