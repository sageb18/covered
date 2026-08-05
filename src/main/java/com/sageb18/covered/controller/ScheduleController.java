package com.sageb18.covered.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScheduleController {

    @PostMapping("/solve")
    public String solve() {
        return "solve endpoint is working";
    }
}
