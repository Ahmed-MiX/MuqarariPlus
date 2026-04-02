package com.muqarariplus.platform.controller;

import com.muqarariplus.platform.entity.Course;
import com.muqarariplus.platform.repository.CourseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ═══════════════════════════════════════════════════════════════════
 * OMNI-SHIELD CONTROLLER TEST — API & UI Ghost Data Prevention
 * ═══════════════════════════════════════════════════════════════════
 * Uses MockMvc to verify:
 * 1. The catalog endpoint (/courses) never sends ghost enrichment counts
 *    to non-technical courses.
 * 2. RBAC enforcement prevents unauthorized roles (STUDENT) from
 *    accessing admin enrichment approval/rejection endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OmniShieldControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private CourseRepository courseRepository;

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
    // TEST 1: Catalog Ghost Data Prevention via Model Inspection
    // ═══════════════════════════════════════════════════════════════
    @Test
    @DisplayName("GHOST SHIELD: Non-technical courses show ZERO approved enrichments in /courses catalog model")
    @SuppressWarnings("unchecked")
    void preventGhostDataInCatalog() throws Exception {
        MvcResult result = mockMvc.perform(get("/courses"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("courses", "approvedCounts"))
            .andReturn();

        List<Course> courses = (List<Course>) result.getModelAndView().getModel().get("courses");
        Map<Long, Long> approvedCounts = (Map<Long, Long>) result.getModelAndView().getModel().get("approvedCounts");

        assertNotNull(courses, "Courses list must not be null");
        assertNotNull(approvedCounts, "ApprovedCounts map must not be null");
        assertFalse(courses.isEmpty(), "Course catalog must not be empty");

        int nonTechVerified = 0;
        for (Course course : courses) {
            if (isNonTechnical(course)) {
                Long count = approvedCounts.getOrDefault(course.getId(), 0L);
                assertEquals(0L, count,
                    "GHOST STATE IN CATALOG! Non-technical course '"
                    + course.getCode() + " - " + course.getNameAr()
                    + "' reports " + count + " approved enrichments via model. Must be 0.");
                nonTechVerified++;
            }
        }

        assertTrue(nonTechVerified > 0,
            "Test validity: at least one non-technical course must exist in the catalog");

        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║ ✅ OMNI-SHIELD TEST 1: " + nonTechVerified
            + " non-tech courses verified ZERO in catalog    ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
    }

    // ═══════════════════════════════════════════════════════════════
    // TEST 2: RBAC Enforcement — Student Cannot Access Admin Paths
    // ═══════════════════════════════════════════════════════════════
    @Test
    @WithMockUser(username = "attacker@student.com", roles = "STUDENT")
    @DisplayName("RBAC SHIELD: Student role receives 403 Forbidden on all admin enrichment endpoints")
    void preventUnauthorizedApproval() throws Exception {
        // Attempt to approve an enrichment — MUST be blocked
        mockMvc.perform(post("/admin/enrichments/1/approve"))
            .andExpect(status().isForbidden());

        // Attempt to reject an enrichment — MUST be blocked
        mockMvc.perform(post("/admin/enrichments/1/reject"))
            .andExpect(status().isForbidden());

        // Attempt to view the pending moderation queue — MUST be blocked
        mockMvc.perform(get("/admin/enrichments/pending"))
            .andExpect(status().isForbidden());

        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║ ✅ OMNI-SHIELD TEST 2: RBAC enforced — 403 on all admin paths    ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
    }
}
