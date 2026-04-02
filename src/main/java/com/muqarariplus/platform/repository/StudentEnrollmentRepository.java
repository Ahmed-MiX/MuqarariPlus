package com.muqarariplus.platform.repository;

import com.muqarariplus.platform.entity.StudentEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════════
 * STUDENT ENROLLMENT REPOSITORY — Data Access Layer
 * Provides Spring Data JPA derived queries for StudentEnrollment
 * entity, supporting the Student Nexus & Tech CV Generator.
 * ═══════════════════════════════════════════════════════════════════
 */
@Repository
public interface StudentEnrollmentRepository extends JpaRepository<StudentEnrollment, Long> {

    /**
     * Finds all enrollments for a student by their email address.
     * Used to extract enrolled courses for TechCV generation.
     */
    List<StudentEnrollment> findByUserEmail(String email);

    /**
     * Checks if a student is already enrolled in a specific course.
     * Prevents duplicate enrollment attempts at the service layer.
     */
    boolean existsByUserEmailAndCourseId(String email, Long courseId);
}
