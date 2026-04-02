package com.muqarariplus.platform.audit;

import com.muqarariplus.platform.entity.AuditLog;
import com.muqarariplus.platform.repository.AuditLogRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * ═══════════════════════════════════════════════════════════════════
 * AUDIT SERVICE — Asynchronous audit log persistence engine.
 * Decouples audit logging from the main request thread.
 * ═══════════════════════════════════════════════════════════════════
 */
@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Persists an audit log entry. Can be called synchronously or asynchronously.
     */
    public void log(String actorEmail, String actorRole, String action,
                    String entityName, String entityId, String details) {
        AuditLog entry = new AuditLog(actorEmail, actorRole, action, entityName, entityId, details);
        auditLogRepository.save(entry);
    }

    /**
     * Async variant for non-critical audit trails.
     */
    @Async
    public void logAsync(String actorEmail, String actorRole, String action,
                         String entityName, String entityId, String details) {
        log(actorEmail, actorRole, action, entityName, entityId, details);
    }
}
