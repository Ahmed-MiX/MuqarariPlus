package com.muqarariplus.platform.dto;

import com.muqarariplus.platform.entity.Course;
import com.muqarariplus.platform.entity.ProfessionalCertificate;
import com.muqarariplus.platform.entity.Tool;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * ═══════════════════════════════════════════════════════════════════
 * TECH CV DTO — The Student's Dynamic Professional Portfolio
 * Aggregates the student's enrolled courses, and all unique Tools
 * and ProfessionalCertificates extracted from APPROVED enrichments
 * across those courses.
 * ═══════════════════════════════════════════════════════════════════
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechCvDTO {

    /**
     * Student's full display name (firstName + lastName).
     */
    private String studentName;

    /**
     * Student's email address.
     */
    private String email;

    /**
     * All courses the student is enrolled in.
     */
    private List<Course> enrolledCourses;

    /**
     * Distinct set of Tools extracted from all APPROVED CourseEnrichments
     * across all enrolled courses.
     */
    private Set<Tool> extractedTools;

    /**
     * Distinct set of ProfessionalCertificates extracted from all APPROVED
     * CourseEnrichments across all enrolled courses.
     */
    private Set<ProfessionalCertificate> extractedCertificates;
}
