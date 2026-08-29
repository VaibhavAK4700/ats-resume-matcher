package com.ats.controller;

import com.ats.scheduler.JobAutomationScheduler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/automation")
public class AutomationController {

    private final JobAutomationScheduler scheduler;

    public AutomationController(JobAutomationScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @PostMapping("/run-search")
    public ResponseEntity<String> triggerCustomSearch(
            @RequestParam(defaultValue = "Künstliche Intelligenz") String query,
            @RequestParam(defaultValue = "Rosenheim, Germany") String location,
            @RequestParam(defaultValue = "50") int radius,
            @RequestParam(defaultValue = "100") int maxResults) {

        scheduler.runPipelineWithParams(query, location, radius, maxResults);
        return ResponseEntity.ok(String.format("Job search executed for '%s' in '%s' (Radius: %d mi, Target: %d jobs).", 
                query, location, radius, maxResults));
    }
}