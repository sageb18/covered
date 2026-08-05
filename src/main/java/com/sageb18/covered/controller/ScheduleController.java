package com.sageb18.covered.controller;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScheduleController {

    @PostMapping("/solve")
    public Map<String, Object> solve() {
        return Map.of(
                "status", "FEASIBLE",
                "assignments", List.of(
                        Map.of("shift", "Monday AM", "employee", "Alex"),
                        Map.of("shift", "Monday PM", "employee", "Sam")
                )
        );
    }
}