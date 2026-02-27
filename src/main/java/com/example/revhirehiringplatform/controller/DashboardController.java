package com.example.revhirehiringplatform.controller;



import com.example.revhirehiringplatform.repository.JobPostRepository;
import com.example.revhirehiringplatform.repository.ApplicationRepository;
import com.example.revhirehiringplatform.repository.UserRepository;
import com.example.revhirehiringplatform.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final JobPostRepository jobPostRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    @GetMapping("/admin/metrics")
    public ResponseEntity<Map<String, Object>> getAdminMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalUsers", userRepository.count());
        metrics.put("totalJobs", jobPostRepository.count());
        metrics.put("totalApplications", applicationRepository.count());
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/employer/metrics")
    public ResponseEntity<Map<String, Object>> getEmployerMetrics(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Map<String, Object> metrics = new HashMap<>();

        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/seeker/metrics")
    public ResponseEntity<Map<String, Object>> getSeekerMetrics(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Map<String, Object> metrics = new HashMap<>();

        metrics.put("seekerId", userDetails.getId());
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/system/health")
    public ResponseEntity<Map<String, String>> getSystemHealth() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("database", "CONNECTED");
        return ResponseEntity.ok(status);
    }

    @GetMapping("/system/logs")
    public ResponseEntity<java.util.List<String>> getSystemLogs() {
        return ResponseEntity.ok(java.util.List.of("System started", "Database connected"));
    }
}
