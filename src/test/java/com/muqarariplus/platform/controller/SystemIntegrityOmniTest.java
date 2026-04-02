package com.muqarariplus.platform.controller;

import com.muqarariplus.platform.entity.*;
import com.muqarariplus.platform.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ═══════════════════════════════════════════════════════════════════
 * SYSTEM INTEGRITY OMNI-TEST — The Master Inquisition Suite
 * ═══════════════════════════════════════════════════════════════════
 * Exhaustive integration tests that sweep the entire database to verify:
 * 1. Absolute purge completeness (zero non-tech enrichments).
 * 2. Expert-domain alignment strictness (no cross-domain bleeding).
 * 3. Iron Vault @PrePersist physical blocking proof.
 */
@SpringBootTest
@Transactional
class SystemIntegrityOmniTest {

    @Autowired private CourseRepository courseRepository;
    @Autowired private CourseEnrichmentRepository enrichmentRepository;
    @Autowired private ExpertRepository expertRepository;

    // ── Non-technical keywords (mirrors DatabaseSeeder.NON_TECH_KEYWORDS) ──
    private static final String[] NON_TECH_KEYWORDS = {
        "ثقافة", "سلم", "إسلام", "عرب", "لغة", "مهارات", "قرآن", "تحرير",
        "islamic", "arabic", "english", "communication", "writing", "reading"
    };

    private boolean isNonTechnical(Course c) {
        String combined = (c.getNameEn() + " " + c.getNameAr()).toLowerCase();
        for (String kw : NON_TECH_KEYWORDS) {
            if (combined.contains(kw.toLowerCase())) return true;
        }
        return false;
    }

    // ═══════════════════════════════════════════════════════════════
    // TEST 3: Absolute Purge — Full DB Scan
    // ═══════════════════════════════════════════════════════════════
    @Test
    @DisplayName("ABSOLUTE PURGE: findAll() returns ZERO enrichments linked to any non-technical course")
    void verifyAbsolutePurge() {
        List<CourseEnrichment> allEnrichments = enrichmentRepository.findAll();
        assertFalse(allEnrichments.isEmpty(),
            "Enrichment table must not be empty for this test to be valid");

        // Build set of non-technical course IDs for O(1) lookup
        Set<Long> nonTechCourseIds = courseRepository.findAll().stream()
            .filter(this::isNonTechnical)
            .map(Course::getId)
            .collect(Collectors.toSet());

        assertFalse(nonTechCourseIds.isEmpty(),
            "Non-technical courses must exist in DB for test validity");

        int violations = 0;
        StringBuilder violationLog = new StringBuilder();

        for (CourseEnrichment enrichment : allEnrichments) {
            if (nonTechCourseIds.contains(enrichment.getCourse().getId())) {
                violations++;
                violationLog.append("\n  VIOLATION: Enrichment #")
                    .append(enrichment.getId())
                    .append(" → Course [")
                    .append(enrichment.getCourse().getCode())
                    .append(" - ").append(enrichment.getCourse().getNameAr())
                    .append("] Status=").append(enrichment.getStatus());
            }
        }

        assertEquals(0, violations,
            "PURGE FAILURE: Found " + violations
            + " enrichment(s) linked to non-technical courses:" + violationLog);

        System.out.println("╔═══════════════════════════════════════════════════════════════════╗");
        System.out.println("║ ✅ OMNI-TEST 3: " + allEnrichments.size()
            + " enrichments scanned — ZERO linked to non-tech courses  ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════════╝");
    }

    // ═══════════════════════════════════════════════════════════════
    // TEST 4: Expert Domain Strictness
    // ═══════════════════════════════════════════════════════════════
    @Test
    @DisplayName("DOMAIN STRICTNESS: SDAIA experts ONLY in AI/Math courses; No security tools in DB/Math domains")
    void verifyExpertDomainStrictness() {
        List<CourseEnrichment> approvedEnrichments = enrichmentRepository
            .findByStatus(EnrichmentStatus.APPROVED);
        assertFalse(approvedEnrichments.isEmpty(),
            "There must be APPROVED enrichments in the database");

        // ── Rule A: SDAIA AI Lead must only enrich AI/Data/Math courses ──
        // NOTE: dalal.ui@sdaia.gov.sa is a UX/UI Designer assigned to SOFTWARE domain
        //       in the seeder, so she is NOT restricted to AI courses.
        Set<String> sdaiaAiEmails = Set.of("khalid.ai@sdaia.gov.sa");
        Set<String> aiDataMathKeywords = Set.of(
            "artificial intelligence", "ذكاء", "image", "صور",
            "signal", "إشارات", "soft computing", "simulation",
            "modeling", "نمذجة", "محاكاة",
            "math", "رياضيات", "calculus", "تفاضل", "تكامل",
            "algebra", "جبر", "statist", "إحصاء", "احتمال",
            "discrete", "متقطعة", "number theory", "أعداد",
            "differential eq", "معادلات", "physics", "فيزياء"
        );

        int sdaiaChecked = 0;
        for (CourseEnrichment e : approvedEnrichments) {
            if (e.getExpert() == null || e.getExpert().getUser() == null) continue;
            String expertEmail = e.getExpert().getUser().getEmail();

            if (sdaiaAiEmails.contains(expertEmail)) {
                Course course = e.getCourse();
                String courseName = (course.getNameEn() + " " + course.getNameAr()).toLowerCase();
                boolean isValidDomain = aiDataMathKeywords.stream()
                    .anyMatch(courseName::contains);

                assertTrue(isValidDomain,
                    "DOMAIN BLEED: SDAIA expert '" + expertEmail
                    + "' is enriching non-AI/Math course: "
                    + course.getCode() + " - " + course.getNameEn());
                sdaiaChecked++;
            }
        }

        // ── Rule B: Security tools must NOT appear in Database or Math course enrichments ──
        Set<String> securityToolNames = Set.of(
            "Kali Linux", "Wireshark", "Metasploit", "Burp Suite"
        );
        Set<String> dbMathKeywords = Set.of(
            "database", "قواعد بيانات",
            "math", "رياضيات", "calculus", "تفاضل", "تكامل",
            "algebra", "جبر", "statist", "إحصاء", "discrete", "متقطعة"
        );

        int crossDomainToolChecks = 0;
        for (CourseEnrichment e : approvedEnrichments) {
            Course course = e.getCourse();
            String courseName = (course.getNameEn() + " " + course.getNameAr()).toLowerCase();
            boolean isDbOrMath = dbMathKeywords.stream().anyMatch(courseName::contains);

            if (isDbOrMath) {
                for (Tool tool : e.getTools()) {
                    assertFalse(securityToolNames.contains(tool.getNameEn()),
                        "CROSS-DOMAIN CONTAMINATION: Security tool '"
                        + tool.getNameEn() + "' found in DB/Math course '"
                        + course.getCode() + " - " + course.getNameEn() + "'");
                    crossDomainToolChecks++;
                }
            }
        }

        System.out.println("╔═══════════════════════════════════════════════════════════════════╗");
        System.out.println("║ ✅ OMNI-TEST 4: " + sdaiaChecked + " SDAIA checks | "
            + crossDomainToolChecks + " tool cross-domain checks passed     ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════════╝");
    }

    // ═══════════════════════════════════════════════════════════════
    // TEST 5: Iron Vault Trigger Proof — @PrePersist Physical Block
    // ═══════════════════════════════════════════════════════════════
    @Test
    @DisplayName("IRON VAULT TRIGGER: Saving enrichment to 'Islamic Culture' course throws CRITICAL SECURITY VIOLATION")
    void verifyIronVaultTriggers() {
        // ── Locate a non-technical course ──
        Course nonTechCourse = courseRepository.findAll().stream()
            .filter(c -> c.getNameEn().toLowerCase().contains("islamic culture"))
            .findFirst()
            .orElse(null);
        assertNotNull(nonTechCourse, "Islamic Culture course must exist in the database");

        // ── Locate an approved expert ──
        Expert expert = expertRepository.findAll().stream()
            .filter(e -> e.getStatus() == ExpertStatus.APPROVED)
            .findFirst()
            .orElse(null);
        assertNotNull(expert, "At least one APPROVED expert must exist in the database");

        // ── Build a fake enrichment targeting the FORBIDDEN course ──
        CourseEnrichment fakeEnrichment = new CourseEnrichment();
        fakeEnrichment.setExpert(expert);
        fakeEnrichment.setCourse(nonTechCourse);
        fakeEnrichment.setContent(
            "FAKE: Docker containerization applied to Islamic Culture — "
            + "This MUST be blocked by the Iron Vault @PrePersist constraint.");
        fakeEnrichment.setStatus(EnrichmentStatus.PENDING);

        // ── The Iron Vault MUST block this persist operation ──
        Exception thrown = assertThrows(Exception.class, () -> {
            enrichmentRepository.saveAndFlush(fakeEnrichment);
        }, "IRON VAULT FAILED! The enrichment was persisted to a non-technical course.");

        // ── Walk the exception chain to find our CRITICAL SECURITY VIOLATION ──
        boolean isVaultViolation = false;
        Throwable cause = thrown;
        while (cause != null) {
            if (cause instanceof IllegalStateException
                && cause.getMessage() != null
                && cause.getMessage().contains("CRITICAL SECURITY VIOLATION")) {
                isVaultViolation = true;
                break;
            }
            cause = cause.getCause();
        }

        assertTrue(isVaultViolation,
            "Expected IllegalStateException with 'CRITICAL SECURITY VIOLATION' but got: "
            + thrown.getClass().getName() + " → " + thrown.getMessage());

        System.out.println("╔═══════════════════════════════════════════════════════════════════╗");
        System.out.println("║ ✅ OMNI-TEST 5: Iron Vault ACTIVATED — persistence BLOCKED!        ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════════╝");
    }
}
