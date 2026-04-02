package com.muqarariplus.platform.controller;

import com.muqarariplus.platform.entity.AuditLog;
import com.muqarariplus.platform.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * ═══════════════════════════════════════════════════════════════════
 * AUDIT REST CONTROLLER — Super Admin God-Eye API
 * Security: ONLY accessible by ROLE_SUPER_ADMIN (enforced in SecurityConfig)
 * ═══════════════════════════════════════════════════════════════════
 */
@RestController
@RequestMapping("/api/admin/audit")
public class AuditRestController {

    private final AuditLogRepository auditLogRepository;

    public AuditRestController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * GET /api/admin/audit?page=0&size=20
     * Returns paginated audit trail ordered by timestamp desc.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditPage = auditLogRepository.findAllByOrderByTimestampDesc(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("logs", auditPage.getContent());
        response.put("currentPage", auditPage.getNumber());
        response.put("totalItems", auditPage.getTotalElements());
        response.put("totalPages", auditPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/admin/audit/actor/{email}
     * Returns all logs for a specific actor.
     */
    @GetMapping("/actor/{email}")
    public ResponseEntity<?> getLogsByActor(@PathVariable String email) {
        return ResponseEntity.ok(auditLogRepository.findByActorEmailOrderByTimestampDesc(email));
    }

    /**
     * GET /api/admin/audit/entity/{name}/{id}
     * Returns all logs for a specific entity.
     */
    @GetMapping("/entity/{name}/{id}")
    public ResponseEntity<?> getLogsByEntity(@PathVariable String name, @PathVariable String id) {
        return ResponseEntity.ok(auditLogRepository.findByEntityNameAndEntityIdOrderByTimestampDesc(name, id));
    }
}
