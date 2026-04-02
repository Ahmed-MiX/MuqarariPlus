package com.muqarariplus.platform.seeder;

import com.muqarariplus.platform.entity.*;
import com.muqarariplus.platform.repository.CourseRepository;
import com.muqarariplus.platform.repository.CourseEnrichmentRepository;
import com.muqarariplus.platform.repository.ExpertRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class DatabaseSeederVerificationTest {

    @Autowired private CourseRepository courseRepository;
    @Autowired private CourseEnrichmentRepository enrichmentRepository;

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
    @DisplayName("PHASE 1 PROOF: Non-technical courses have ZERO approved enrichments")
    void verifyNonTechnicalCoursesAreEmpty() {
        List<Course> allCourses = courseRepository.findAll();
        List<Course> nonTechCourses = allCourses.stream()
                .filter(this::isNonTechnical)
                .collect(Collectors.toList());

        for (Course course : nonTechCourses) {
            List<CourseEnrichment> approved = enrichmentRepository
                    .findByCourseIdAndStatus(course.getId(), EnrichmentStatus.APPROVED);
            assertEquals(0, approved.size());
        }
    }
}