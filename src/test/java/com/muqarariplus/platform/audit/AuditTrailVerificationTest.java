package com.muqarariplus.platform.audit;

import com.muqarariplus.platform.entity.*;
import com.muqarariplus.platform.repository.*;
import com.muqarariplus.platform.service.CourseEnrichmentService;
import com.muqarariplus.platform.service.EngagementService;
import com.muqarariplus.platform.service.ExpertService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * THE PANOPTICON VERIFICATION TEST
 * Proves that the AOP Audit Trail silently intercepts and logs every critical action.
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuditTrailVerificationTest {

    @Autowired private AuditLogRepository auditLogRepository;
    @Autowired private CourseEnrichmentService enrichmentService;
    @Autowired private EngagementService engagementService;
    @Autowired private CourseEnrichmentRepository enrichmentRepository;
    @Autowired private UserRepository userRepository;

    @BeforeEach
    void clearAuditLogs() {
        auditLogRepository.deleteAll();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ── Helper: Set a fake authenticated user in SecurityContext ──
    private void authenticateAs(String email, String role) {
        var auth = new UsernamePasswordAuthenticationToken(
            email, "password",
            List.of(new SimpleGrantedAuthority(role))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ═══════════════════════════════════════════════════════════════
    // TEST 1: Admin APPROVE action generates an AuditLog
    // ═══════════════════════════════════════════════════════════════
    @Test
    @Order(1)
    @DisplayName("Test 1: Admin APPROVE enrichment → AuditLog generated")
    void testApproveEnrichmentCreatesAuditLog() {
        // Find any APPROVED enrichment to test with (re-approve it)
        List<CourseEnrichment> approved = enrichmentRepository
            .findByStatus(EnrichmentStatus.APPROVED);
        Assumptions.assumeFalse(approved.isEmpty(), "No approved enrichments to test with");

        CourseEnrichment target = approved.get(0);
        Long targetId = target.getId();

        // Simulate admin login
        authenticateAs("admin@psau.edu.sa", "ROLE_ADMIN");

        // Perform the action
        enrichmentService.approveEnrichment(targetId);

        // Verify audit log was created
        List<AuditLog> logs = auditLogRepository.findByActionOrderByTimestampDesc("APPROVE");
        assertFalse(logs.isEmpty(), "╔═══ FAIL: No APPROVE audit log found! ═══╗");

        AuditLog log = logs.get(0);
        assertEquals("admin@psau.edu.sa", log.getActorEmail(),
            "Actor email must be the admin who performed the action");
        assertEquals("ROLE_ADMIN", log.getActorRole(),
            "Actor role must be ROLE_ADMIN");
        assertEquals("APPROVE", log.getAction(),
            "Action must be APPROVE");
        assertEquals("CourseEnrichment", log.getEntityName(),
            "Entity name must be CourseEnrichment");
        assertEquals(String.valueOf(targetId), log.getEntityId(),
            "Entity ID must match the enrichment ID");
        assertNotNull(log.getTimestamp(), "Timestamp must not be null");
        assertNotNull(log.getDetails(), "Details must not be null");

        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║ ✅ TEST 1 PASSED: APPROVE action audited correctly    ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
    }

    // ═══════════════════════════════════════════════════════════════
    // TEST 2: Admin REJECT action generates an AuditLog
    // ═══════════════════════════════════════════════════════════════
    @Test
    @Order(2)
    @DisplayName("Test 2: Admin REJECT enrichment → AuditLog generated")
    void testRejectEnrichmentCreatesAuditLog() {
        List<CourseEnrichment> approved = enrichmentRepository
            .findByStatus(EnrichmentStatus.APPROVED);
        Assumptions.assumeFalse(approved.isEmpty(), "No enrichments to test with");

        CourseEnrichment target = approved.get(0);
        Long targetId = target.getId();

        authenticateAs("admin@psau.edu.sa", "ROLE_ADMIN");
        enrichmentService.rejectEnrichment(targetId);

        List<AuditLog> logs = auditLogRepository.findByActionOrderByTimestampDesc("REJECT");
        assertFalse(logs.isEmpty(), "╔═══ FAIL: No REJECT audit log found! ═══╗");

        AuditLog log = logs.get(0);
        assertEquals("REJECT", log.getAction());
        assertEquals("CourseEnrichment", log.getEntityName());
        assertEquals("admin@psau.edu.sa", log.getActorEmail());

        // Restore the enrichment to APPROVED state
        target.setStatus(EnrichmentStatus.APPROVED);
        enrichmentRepository.save(target);

        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║ ✅ TEST 2 PASSED: REJECT action audited correctly     ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
    }

    // ═══════════════════════════════════════════════════════════════
    // TEST 3: Student TOGGLE_UPVOTE action generates an AuditLog
    // ═══════════════════════════════════════════════════════════════
    @Test
    @Order(3)
    @DisplayName("Test 3: Student TOGGLE_UPVOTE → AuditLog generated")
    void testToggleUpvoteCreatesAuditLog() {
        List<CourseEnrichment> approved = enrichmentRepository
            .findByStatus(EnrichmentStatus.APPROVED);
        Assumptions.assumeFalse(approved.isEmpty(), "No enrichments to test with");

        // Find a student user
        List<User> students = userRepository.findByRole("ROLE_STUDENT");
        Assumptions.assumeFalse(students.isEmpty(), "No students to test with");

        CourseEnrichment target = approved.get(0);
        User student = students.get(0);

        authenticateAs(student.getEmail(), "ROLE_STUDENT");
        engagementService.toggleUpvote(target.getId(), student.getEmail());

        List<AuditLog> logs = auditLogRepository.findByActionOrderByTimestampDesc("TOGGLE_UPVOTE");
        assertFalse(logs.isEmpty(), "╔═══ FAIL: No TOGGLE_UPVOTE audit log found! ═══╗");

        AuditLog log = logs.get(0);
        assertEquals("TOGGLE_UPVOTE", log.getAction());
        assertEquals(student.getEmail(), log.getActorEmail());
        assertEquals("ROLE_STUDENT", log.getActorRole());

        // Toggle back to clean up
        engagementService.toggleUpvote(target.getId(), student.getEmail());

        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║ ✅ TEST 3 PASSED: TOGGLE_UPVOTE audited correctly     ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
    }

    // ═══════════════════════════════════════════════════════════════
    // TEST 4: SYSTEM actor logged when no SecurityContext
    // ═══════════════════════════════════════════════════════════════
    @Test
    @Order(4)
    @DisplayName("Test 4: No SecurityContext → actor logged as SYSTEM")
    void testSystemActorWhenNoAuthentication() {
        List<CourseEnrichment> approved = enrichmentRepository
            .findByStatus(EnrichmentStatus.APPROVED);
        Assumptions.assumeFalse(approved.isEmpty(), "No enrichments to test with");

        SecurityContextHolder.clearContext(); // No user logged in
        CourseEnrichment target = approved.get(0);
        enrichmentService.approveEnrichment(target.getId());

        List<AuditLog> logs = auditLogRepository.findByActionOrderByTimestampDesc("APPROVE");
        assertFalse(logs.isEmpty());

        AuditLog log = logs.get(0);
        assertEquals("SYSTEM", log.getActorEmail(),
            "When no user is authenticated, actor must be SYSTEM");
        assertEquals("SYSTEM", log.getActorRole());

        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║ ✅ TEST 4 PASSED: SYSTEM actor logged correctly       ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
    }

    // ═══════════════════════════════════════════════════════════════
    // TEST 5: Audit log details contain method arguments
    // ═══════════════════════════════════════════════════════════════
    @Test
    @Order(5)
    @DisplayName("Test 5: Audit details contain method arguments")
    void testAuditDetailsContainArguments() {
        List<CourseEnrichment> approved = enrichmentRepository
            .findByStatus(EnrichmentStatus.APPROVED);
        Assumptions.assumeFalse(approved.isEmpty(), "No enrichments to test with");

        authenticateAs("superadmin@psau.edu.sa", "ROLE_SUPER_ADMIN");
        Long targetId = approved.get(0).getId();
        enrichmentService.approveEnrichment(targetId);

        List<AuditLog> logs = auditLogRepository.findByActionOrderByTimestampDesc("APPROVE");
        assertFalse(logs.isEmpty());

        String details = logs.get(0).getDetails();
        assertNotNull(details);
        assertTrue(details.contains("approveEnrichment"),
            "Details must contain method name. Got: " + details);
        assertTrue(details.contains(String.valueOf(targetId)),
            "Details must contain the enrichment ID. Got: " + details);

        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║ ✅ TEST 5 PASSED: Audit details contain arguments     ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
    }
}
