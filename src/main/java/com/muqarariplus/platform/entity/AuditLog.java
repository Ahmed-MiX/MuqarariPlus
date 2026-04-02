package com.muqarariplus.platform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * ═══════════════════════════════════════════════════════════════════
 * THE PANOPTICON AUDIT LOG — Every action, permanently recorded.
 * ═══════════════════════════════════════════════════════════════════
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_actor", columnList = "actorEmail"),
    @Index(name = "idx_audit_action", columnList = "action")
})
@Getter @Setter
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Who performed the action (email or "SYSTEM") */
    @Column(nullable = false)
    private String actorEmail;

    /** Role of the actor (e.g., ROLE_ADMIN, ROLE_EXPERT, SYSTEM) */
    @Column(nullable = false)
    private String actorRole;

    /** Action type: CREATE, APPROVE, REJECT, DELETE, TOGGLE_UPVOTE, etc. */
    @Column(nullable = false, length = 50)
    private String action;

    /** Entity class name (e.g., CourseEnrichment, Expert, User) */
    @Column(nullable = false)
    private String entityName;

    /** ID of the affected entity */
    @Column
    private String entityId;

    /** Detailed description of what changed (JSON or text) */
    @Column(columnDefinition = "TEXT")
    private String details;

    /** Timestamp of the action */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    public AuditLog(String actorEmail, String actorRole, String action,
                    String entityName, String entityId, String details) {
        this.actorEmail = actorEmail;
        this.actorRole = actorRole;
        this.action = action;
        this.entityName = entityName;
        this.entityId = entityId;
        this.details = details;
    }
}
