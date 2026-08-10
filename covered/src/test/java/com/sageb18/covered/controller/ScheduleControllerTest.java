package com.sageb18.covered.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sageb18.covered.dto.AssignmentDto;
import com.sageb18.covered.dto.SolveRequest;
import com.sageb18.covered.dto.SolveResponse;
import com.sageb18.covered.dto.ViolationDto;
import com.sageb18.covered.service.DemoScenario;
import com.sageb18.covered.service.SchedulingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web layer only -- the solver is mocked, so these are about status codes and JSON shape.
 */
@WebMvcTest(ScheduleController.class)
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SchedulingService schedulingService;

    @Test
    @DisplayName("POST /api/solve returns the solved schedule")
    void solveReturnsResponse() throws Exception {
        UUID shiftId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        given(schedulingService.solve(any())).willReturn(new SolveResponse(
                true, 0, 0, List.of(new AssignmentDto(shiftId, employeeId)), List.of()));

        mockMvc.perform(post("/api/solve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(DemoScenario.build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feasible").value(true))
                .andExpect(jsonPath("$.assignments[0].shiftId").value(shiftId.toString()))
                .andExpect(jsonPath("$.assignments[0].employeeId").value(employeeId.toString()));
    }

    @Test
    @DisplayName("POST /api/solve reports violations when infeasible")
    void solveReturnsViolations() throws Exception {
        given(schedulingService.solve(any())).willReturn(new SolveResponse(
                false, -2, 0, List.of(), List.of(new ViolationDto("Missing required skill", 2))));

        mockMvc.perform(post("/api/solve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(DemoScenario.build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feasible").value(false))
                .andExpect(jsonPath("$.hardScore").value(-2))
                .andExpect(jsonPath("$.violations[0].constraint").value("Missing required skill"))
                .andExpect(jsonPath("$.violations[0].count").value(2));
    }

    @Test
    @DisplayName("an empty employee list is a 400, not a 500")
    void rejectsEmptyEmployees() throws Exception {
        String json = """
                {"employees": [], "shifts": []}
                """;

        mockMvc.perform(post("/api/solve").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request is not valid"))
                .andExpect(jsonPath("$.details").isNotEmpty());
    }

    @Test
    @DisplayName("a shift ending before it starts is a 400")
    void rejectsBackwardsShift() throws Exception {
        String json = """
                {
                  "employees": [{"id":"00000000-0000-0000-0000-0000000000a1","name":"Sage","maxHours":20}],
                  "shifts": [{"id":"00000000-0000-0000-0000-0000000000b1","dayOfWeek":"MONDAY",
                              "start":"17:00","end":"09:00","requiredSkill":"BARISTA"}]
                }
                """;

        mockMvc.perform(post("/api/solve").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value(
                        org.hamcrest.Matchers.containsString("end must be after start")));
    }

    @Test
    @DisplayName("a duplicate id from the service surfaces as a 400")
    void duplicateIdIsBadRequest() throws Exception {
        willThrow(new IllegalArgumentException("Duplicate employee id abc"))
                .given(schedulingService).solve(any());

        mockMvc.perform(post("/api/solve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(DemoScenario.build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Duplicate employee id abc"));
    }

    @Test
    @DisplayName("GET /api/demo-scenario returns a payload postable straight back to /api/solve")
    void demoScenarioRoundTrips() throws Exception {
        String body = mockMvc.perform(get("/api/demo-scenario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employees").isNotEmpty())
                .andExpect(jsonPath("$.shifts").isNotEmpty())
                .andExpect(jsonPath("$.shifts[0].start").value("09:00"))
                .andReturn().getResponse().getContentAsString();

        SolveRequest parsed = objectMapper.readValue(body, SolveRequest.class);
        given(schedulingService.solve(any())).willReturn(
                new SolveResponse(true, 0, 0, List.of(), List.of()));

        mockMvc.perform(post("/api/solve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(parsed)))
                .andExpect(status().isOk());
    }
}
