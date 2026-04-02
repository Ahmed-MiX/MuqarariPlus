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

@SpringBootTest
@AutoConfigureMockMvc
class OmniShieldControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private CourseRepository courseRepository;

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

        assertNotNull(courses);
        assertNotNull(approvedCounts);

        int nonTechVerified = 0;
        for (Course course : courses) {
            if (isNonTechnical(course)) {
                Long count = approvedCounts.getOrDefault(course.getId(), 0L);
                assertEquals(0L, count);
                nonTechVerified++;
            }
        }
        assertTrue(nonTechVerified > 0);
    }

    @Test
    @WithMockUser(username = "attacker@student.com", roles = "STUDENT")
    @DisplayName("RBAC SHIELD: Student role receives 403 Forbidden on all admin enrichment endpoints")
    void preventUnauthorizedApproval() throws Exception {
        mockMvc.perform(post("/admin/enrichments/1/approve"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/admin/enrichments/1/reject"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/admin/enrichments/pending"))
                .andExpect(status().isForbidden());
    }
}