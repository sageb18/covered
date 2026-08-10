package com.sageb18.covered.controller;

import com.sageb18.covered.dto.SolveRequest;
import com.sageb18.covered.dto.SolveResponse;
import com.sageb18.covered.service.DemoScenario;
import com.sageb18.covered.service.SchedulingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Everything under /api, so the built React app can own the rest of the URL space.
 */
@RestController
@RequestMapping("/api")
public class ScheduleController {

    private final SchedulingService schedulingService;

    public ScheduleController(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @PostMapping("/solve")
    public SolveResponse solve(@Valid @RequestBody SolveRequest request) {
        return schedulingService.solve(request);
    }

    @GetMapping("/demo-scenario")
    public SolveRequest demoScenario() {
        return DemoScenario.build();
    }
}
