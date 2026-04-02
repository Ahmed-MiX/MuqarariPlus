package com.muqarariplus.platform.audit;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * THE SILENT WATCHER — Spring AOP Audit Aspect (The Panopticon Core).
 * Intercepts:
 *   1. Any method annotated with @Auditable (explicit tagging)
 *   2. Service methods matching save, update, delete, approve, reject, toggle (convention)
 * Extracts the current actor from SecurityContextHolder.
 * Persists an AuditLog entry for every intercepted action.
 */
@Aspect
@Component
public class AuditAspect {

    private final AuditService auditService;

    public AuditAspect(AuditService auditService) {
        this.auditService = auditService;
    }

    // ═══════════════════════════════════════════════════════════════
    // POINTCUT 1: Methods explicitly annotated with @Auditable
    // ═══════════════════════════════════════════════════════════════
    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void auditAnnotatedMethod(JoinPoint joinPoint, Auditable auditable, Object result) {
        String[] actor = extractActor();
        String action = auditable.action();
        String entity = auditable.entity().isEmpty()
                ? joinPoint.getTarget().getClass().getSimpleName().replace("Service", "")
                : auditable.entity();
        String entityId = extractEntityId(joinPoint.getArgs());
        String details = buildDetails(joinPoint);

        auditService.log(actor[0], actor[1], action, entity, entityId, details);
    }

    // ═══════════════════════════════════════════════════════════════
    // POINTCUT 2: Convention-based — service methods that alter data
    // Matches: save*, update*, delete*, approve*, reject*, toggle*, create*, submit*
    // in the service package. Excludes methods already tagged with @Auditable
    // to prevent double-logging.
    // ═══════════════════════════════════════════════════════════════
    @AfterReturning(
        pointcut = "execution(* com.muqarariplus.platform.service..*.save*(..)) || " +
                   "execution(* com.muqarariplus.platform.service..*.update*(..)) || " +
                   "execution(* com.muqarariplus.platform.service..*.delete*(..)) || " +
                   "execution(* com.muqarariplus.platform.service..*.create*(..)) || " +
                   "execution(* com.muqarariplus.platform.service..*.submit*(..)) || " +
                   "execution(* com.muqarariplus.platform.service..*.toggle*(..)) || " +
                   "execution(* com.muqarariplus.platform.service..*.approve*(..)) || " +
                   "execution(* com.muqarariplus.platform.service..*.reject*(..))",
        returning = "result"
    )
    public void auditServiceMethods(JoinPoint joinPoint, Object result) {
        // Skip if method is already annotated with @Auditable (prevent double-log)
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        if (signature.getMethod().isAnnotationPresent(Auditable.class)) {
            return;
        }

        String[] actor = extractActor();
        String methodName = signature.getMethod().getName();
        String action = deriveAction(methodName);
        String entity = joinPoint.getTarget().getClass().getSimpleName().replace("Service", "");
        String entityId = extractEntityId(joinPoint.getArgs());
        String details = buildDetails(joinPoint);

        auditService.log(actor[0], actor[1], action, entity, entityId, details);
    }

    // ── Helper: Extract current actor from SecurityContext ──
    private String[] extractActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return new String[]{"SYSTEM", "SYSTEM"};
        }
        String email = auth.getName();
        String role = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("UNKNOWN");
        return new String[]{email, role};
    }

    // ── Helper: Derive action name from method name ──
    private String deriveAction(String methodName) {
        if (methodName.startsWith("approve")) return "APPROVE";
        if (methodName.startsWith("reject")) return "REJECT";
        if (methodName.startsWith("delete")) return "DELETE";
        if (methodName.startsWith("toggle")) return "TOGGLE";
        if (methodName.startsWith("create")) return "CREATE";
        if (methodName.startsWith("submit")) return "SUBMIT";
        if (methodName.startsWith("save")) return "SAVE";
        if (methodName.startsWith("update")) return "UPDATE";
        return methodName.toUpperCase();
    }

    // ── Helper: Try to extract an entity ID from the first argument ──
    private String extractEntityId(Object[] args) {
        if (args == null || args.length == 0) return "N/A";
        Object first = args[0];
        if (first instanceof Long || first instanceof String) {
            return String.valueOf(first);
        }
        return "N/A";
    }

    // ── Helper: Build details string from method signature + args ──
    private String buildDetails(JoinPoint joinPoint) {
        MethodSignature sig = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = sig.getParameterNames();
        Object[] args = joinPoint.getArgs();
        if (paramNames == null || args == null) {
            return sig.getMethod().getName() + "()";
        }
        StringBuilder sb = new StringBuilder(sig.getMethod().getName()).append("(");
        for (int i = 0; i < paramNames.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(paramNames[i]).append("=");
            // Truncate large args to avoid DB overflow
            String val = args[i] != null ? args[i].toString() : "null";
            sb.append(val.length() > 200 ? val.substring(0, 200) + "..." : val);
        }
        sb.append(")");
        return sb.toString();
    }
}
