package com.muqarariplus.platform.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ═══════════════════════════════════════════════════════════════════
 * @Auditable — Tag any service method for automatic audit logging.
 * The AuditAspect intercepts methods annotated with this and logs
 * the action, actor, entity, and details to the AuditLog table.
 * ═══════════════════════════════════════════════════════════════════
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /** The action being performed (e.g., "APPROVE", "REJECT", "CREATE") */
    String action();

    /** The entity name being acted upon (e.g., "CourseEnrichment") */
    String entity() default "";
}
