package com.smartqueue.health.controller;

import com.smartqueue.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {
    @GetMapping()
    public ApiResponse<Map<String, String>> health() {
        return new ApiResponse<>("Service is running", Map.of("status", "UP"), Instant.now());
    }
}
