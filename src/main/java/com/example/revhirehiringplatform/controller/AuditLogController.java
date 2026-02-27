package com.example.revhirehiringplatform.controller;

import com.example.revhirehiringplatform.model.AuditLog;
import com.example.revhirehiringplatform.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Slf4j
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping("/public-test")
    public ResponseEntity<String> publicTest() {
        log.info("Public test endpoint reached");
        return ResponseEntity.ok("Public test successful");
    }

    @GetMapping
    public ResponseEntity<List<AuditLog>> getAllLogs() {
        log.info("ENTERING AuditLogController.getAllLogs()");
        List<AuditLog> logs = auditLogRepository.findAll();
        log.info("Found {} audit logs", logs.size());
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditLog> getLogById(@PathVariable Long id) {
        log.info("Request received to fetch audit log by ID: {}", id);
        return auditLogRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/entity/{entityType}")
    public ResponseEntity<List<AuditLog>> getLogsByEntityType(@PathVariable String entityType) {
        log.info("Request received to fetch audit logs by entity type: {}", entityType);
        return ResponseEntity.ok(auditLogRepository.findByEntityType(entityType));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<List<AuditLog>> getLogsByEntity(@PathVariable String entityType,
            @PathVariable Long entityId) {
        return ResponseEntity
                .ok(auditLogRepository.findByEntityTypeAndEntityIdOrderByChangedAtDesc(entityType, entityId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditLog>> getLogsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(auditLogRepository.findByChangedBy_IdOrderByChangedAtDesc(userId));
    }

    @DeleteMapping("/cleanup")
    public ResponseEntity<Void> cleanupLogs(@RequestParam(required = false) Integer daysAgo) {
        // Implementation for cleanup logic
        return ResponseEntity.ok().build();
    }
}
