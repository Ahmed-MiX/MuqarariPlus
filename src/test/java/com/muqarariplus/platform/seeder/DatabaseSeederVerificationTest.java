package com.muqarariplus.platform.seeder;

import com.muqarariplus.platform.entity.*;
import com.muqarariplus.platform.repository.CourseEnrichmentRepository;
import com.muqarariplus.platform.repository.CourseRepository;
import com.muqarariplus.platform.repository.ExpertRepository;
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
 * ZERO-DEFECT VERIFICATION SUITE for Moqarari+ Deterministic Seeder
 * ═══════════════════════════════════════════════════════════════════
 * These tests run AFTER the seeder has executed (on app startup).
 * They verify the state of the live database to prove correctness.
 */
@SpringBootTest
@Transactional
class DatabaseSeederVerificationTest {

    @Autowired private CourseRepository courseRepository;
    @Autowired private CourseEnrichmentRepository enrichmentRepository;
    @Autowired private ExpertRepository expertRepository;

    // ── Non-technical keywords (must match DatabaseSeeder.NON_TECH_KEYWORDS) ──
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
    // TEST 1: Non-Technical Courses Must Have ZERO Enrichments
    // ═══════════════════════════════════════════════════════════════
    @Test
    @DisplayName("PHASE 1 PROOF: Non-technical courses (Islamic, Arabic, English, Communication) have ZERO approved enrichments")
    void verifyNonTechnicalCoursesAreEmpty() {
        List<Course> allCourses = courseRepository.findAll();
        assertFalse(allCourses.isEmpty(), "Course table must not be empty");

        List<Course> nonTechCourses = allCourses.stream()
            .filter(this::isNonTechnical)
            .collect(Collectors.toList());

        assertFalse(nonTechCourses.isEmpty(),
            "There should be at least some non-technical courses in the DB (IC, ARAB, ENG, COMM)");

        for (Course course : nonTechCourses) {
            List<CourseEnrichment> approved = enrichmentRepository
                .findByCourseIdAndStatus(course.getId(), EnrichmentStatus.APPROVED);

            assertEquals(0, approved.size(),
                "NON-TECHNICAL COURSE '" + course.getCode() + " - " + course.getNameAr()
                + "' MUST have 0 approved enrichments, but found " + approved.size());
        }

        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║ ✅ TEST 1 PASSED: " + nonTechCourses.size()
            + " non-technical courses verified EMPTY.  ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }

    // ═══════════════════════════════════════════════════════════════
    // TEST 2: Domain Logic Integrity — No Cross-Domain Contamination
    // ═══════════════════════════════════════════════════════════════
    @Test
    @DisplayName("PHASE 2 PROOF: Database course enrichments contain ONLY DB-related certs/tools, no CyberSec or Web leakage")
    void verifyDomainLogicIntegrity() {
        // ── Find the Database Systems course ──
        List<Course> allCourses = courseRepository.findAll();
        Course dbCourse = allCourses.stream()
            .filter(c -> c.getNameEn().toLowerCase().contains("database"))
            .findFirst()
            .orElse(null);

        assertNotNull(dbCourse, "A 'Database' course must exist in the DB");

        List<CourseEnrichment> enrichments = enrichmentRepository
            .findByCourseIdAndStatus(dbCourse.getId(), EnrichmentStatus.APPROVED);

        assertFalse(enrichments.isEmpty(),
            "Database course '" + dbCourse.getCode() + "' should have approved enrichments");

        // ── Forbidden cross-domain items for DATABASE domain ──
        Set<String> forbiddenCerts = Set.of(
            "CISSP - Certified Information Systems Security Professional",
            "CEH - Certified Ethical Hacker",
            "CompTIA Security+",
            "Certified ScrumMaster (CSM)",
            "ISTQB Certified Tester"
        );
        Set<String> forbiddenTools = Set.of(
            "React", "Spring Boot", "Kali Linux", "Metasploit",
            "Wireshark", "Burp Suite", "Flutter", "Figma",
            "Arduino", "Xilinx Vivado", "Simulink"
        );

        for (CourseEnrichment e : enrichments) {
            // Check certs
            for (ProfessionalCertificate cert : e.getCertificates()) {
                assertFalse(forbiddenCerts.contains(cert.getNameEn()),
                    "DATABASE course enrichment #" + e.getId()
                    + " contains FORBIDDEN cross-domain cert: " + cert.getNameEn());
            }
            // Check tools
            for (Tool tool : e.getTools()) {
                assertFalse(forbiddenTools.contains(tool.getNameEn()),
                    "DATABASE course enrichment #" + e.getId()
                    + " contains FORBIDDEN cross-domain tool: " + tool.getNameEn());
            }
        }

        // ── Positive check: at least one enrichment should have a DB-related cert ──
        Set<String> expectedDbCerts = Set.of(
            "Oracle Database SQL Certified Associate",
            "Oracle Certified Professional",
            "MongoDB Developer Certification"
        );
        boolean hasDbCert = enrichments.stream()
            .flatMap(e -> e.getCertificates().stream())
            .anyMatch(c -> expectedDbCerts.contains(c.getNameEn()));

        assertTrue(hasDbCert,
            "At least one DATABASE enrichment must have a DB-relevant certification (Oracle/MongoDB)");

        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║ ✅ TEST 2 PASSED: Domain logic verified for DB course ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }

    // ═══════════════════════════════════════════════════════════════
    // TEST 3: Expert Consistency — AI Expert ≠ Hardware Course
    // ═══════════════════════════════════════════════════════════════
    @Test
    @DisplayName("PHASE 3 PROOF: AI Expert (SDAIA) does NOT enrich Hardware/Circuit courses")
    void verifyExpertConsistency() {
        // ── Find the SDAIA AI expert ──
        Expert aiExpert = expertRepository.findByUserEmail("khalid.ai@sdaia.gov.sa").orElse(null);
        assertNotNull(aiExpert, "SDAIA AI expert (khalid.ai@sdaia.gov.sa) must exist");

        // ── Find all hardware courses (circuits, embedded, VLSI, etc.) ──
        List<Course> hardwareCourses = courseRepository.findAll().stream()
            .filter(c -> {
                String n = (c.getNameEn() + " " + c.getNameAr()).toLowerCase();
                return n.contains("circuit") || n.contains("دوائر")
                    || n.contains("vlsi") || n.contains("متكاملة")
                    || n.contains("embedded") || n.contains("مضمن")
                    || n.contains("control system") || n.contains("تحكم");
            })
            .collect(Collectors.toList());

        assertFalse(hardwareCourses.isEmpty(), "There should be hardware courses in the DB");

        for (Course hwCourse : hardwareCourses) {
            List<CourseEnrichment> enrichments = enrichmentRepository
                .findByCourseIdAndStatus(hwCourse.getId(), EnrichmentStatus.APPROVED);

            for (CourseEnrichment e : enrichments) {
                assertNotEquals(aiExpert.getId(), e.getExpert().getId(),
                    "AI Expert (SDAIA) should NOT be enriching HARDWARE course: "
                    + hwCourse.getCode() + " (" + hwCourse.getNameAr() + ")");
            }
        }

        // ── Positive check: AI expert SHOULD be in AI courses ──
        List<Course> aiCourses = courseRepository.findAll().stream()
            .filter(c -> c.getNameEn().toLowerCase().contains("artificial intelligence"))
            .collect(Collectors.toList());

        if (!aiCourses.isEmpty()) {
            boolean aiExpertInAiCourse = false;
            for (Course aiCourse : aiCourses) {
                List<CourseEnrichment> enrichments = enrichmentRepository
                    .findByCourseIdAndStatus(aiCourse.getId(), EnrichmentStatus.APPROVED);
                for (CourseEnrichment e : enrichments) {
                    if (e.getExpert().getId().equals(aiExpert.getId())) {
                        aiExpertInAiCourse = true;
                        break;
                    }
                }
            }
            assertTrue(aiExpertInAiCourse,
                "AI Expert from SDAIA should be contributing to at least one AI course");
        }

        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║ ✅ TEST 3 PASSED: Expert-domain alignment verified.  ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }

    // ═══════════════════════════════════════════════════════════════
    // TEST 4: Security Course — Verify ONLY Security Skills/Tools
    // ═══════════════════════════════════════════════════════════════
    @Test
    @DisplayName("BONUS: Security course enrichments contain ONLY security-domain tools")
    void verifySecurityDomainIsolation() {
        List<Course> secCourses = courseRepository.findAll().stream()
            .filter(c -> c.getNameEn().toLowerCase().contains("security")
                      || c.getNameAr().contains("أمن"))
            .collect(Collectors.toList());

        if (secCourses.isEmpty()) return; // Skip if no security courses

        Set<String> allowedSecTools = Set.of(
            "Kali Linux", "Wireshark", "Metasploit", "Burp Suite", "Linux",
            "CrowdStrike", "Splunk", "Fortinet FortiGate", "Nmap", "Snort",
            "Nessus", "OWASP ZAP", "Qualys", "Palo Alto Networks",
            "HashiCorp Vault", "Elastic SIEM", "IBM QRadar", "SentinelOne",
            "Ghidra", "OpenVAS", "Suricata"
        );

        for (Course secCourse : secCourses) {
            List<CourseEnrichment> enrichments = enrichmentRepository
                .findByCourseIdAndStatus(secCourse.getId(), EnrichmentStatus.APPROVED);

            for (CourseEnrichment e : enrichments) {
                for (Tool tool : e.getTools()) {
                    assertTrue(allowedSecTools.contains(tool.getNameEn()),
                        "SECURITY course '" + secCourse.getCode()
                        + "' enrichment has non-security tool: " + tool.getNameEn());
                }
            }
        }

        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║ ✅ TEST 4 PASSED: Security domain isolation verified ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }

    // ═══════════════════════════════════════════════════════════════
    // TEST 5: Every Technical Course Has At Least 3 Enrichments
    // ═══════════════════════════════════════════════════════════════
    @Test
    @DisplayName("COVERAGE: Every technical course has at least 3 approved enrichments")
    void verifyMinimumEnrichmentCoverage() {
        List<Course> allCourses = courseRepository.findAll();
        int technicalCount = 0;
        int fullyEnriched = 0;

        for (Course course : allCourses) {
            if (isNonTechnical(course)) continue;
            technicalCount++;

            List<CourseEnrichment> approved = enrichmentRepository
                .findByCourseIdAndStatus(course.getId(), EnrichmentStatus.APPROVED);

            assertTrue(approved.size() >= 3,
                "Technical course '" + course.getCode() + " - " + course.getNameEn()
                + "' has only " + approved.size() + " enrichments (minimum 3 required)");

            fullyEnriched++;
        }

        assertTrue(technicalCount > 0, "There must be technical courses in the system");

        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║ ✅ TEST 5 PASSED: " + fullyEnriched + "/" + technicalCount
            + " technical courses have ≥3 enrichments ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }

    // ═══════════════════════════════════════════════════════════════
    // TEST 6: ANTI-GHOST — Zero Enrichments of ANY Status on Non-Tech
    // ═══════════════════════════════════════════════════════════════
    @Test
    @DisplayName("ANTI-GHOST: Non-technical courses have ZERO enrichments of ANY status (no ghosts)")
    void verifyNoGhostStatesInCatalog() {
        List<Course> allCourses = courseRepository.findAll();

        // ── Specifically target the "Arabic Writing" course and other known non-tech ──
        List<Course> nonTechCourses = allCourses.stream()
            .filter(this::isNonTechnical)
            .collect(Collectors.toList());

        assertFalse(nonTechCourses.isEmpty(),
            "Non-technical courses must exist for this test to be valid");

        int ghostsFound = 0;
        for (Course course : nonTechCourses) {
            // Check ALL statuses — this is the anti-ghost assertion
            List<CourseEnrichment> allEnrichments = enrichmentRepository.findByCourseId(course.getId());

            assertEquals(0, allEnrichments.size(),
                "GHOST STATE DETECTED! Non-technical course '" + course.getCode()
                + " - " + course.getNameAr() + "' has " + allEnrichments.size()
                + " enrichment(s) of various statuses. Expected: 0 (ZERO).");

            // Also verify the approved count method returns 0
            long approvedCount = enrichmentRepository.countByCourseIdAndStatus(
                course.getId(), EnrichmentStatus.APPROVED);
            assertEquals(0L, approvedCount,
                "GHOST BADGE! Approved count for non-technical course '"
                + course.getCode() + "' should be 0 but was " + approvedCount);
        }

        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║ ✅ TEST 6 PASSED: " + nonTechCourses.size()
            + " non-tech courses have ZERO ghost data  ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }
}
