package com.muqarariplus.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ═══════════════════════════════════════════════════════════════════
 * STUDENT ENROLLMENT — Audit-Grade Enrollment Record Entity
 * Provides a first-class JPA entity for tracking student course
 * enrollments with timestamp metadata. Complements the existing
 * User.enrolledCourses @ManyToMany by adding full audit capability.
 * ═══════════════════════════════════════════════════════════════════
 */
@Entity
@Table(name = "student_enrollments",
       uniqueConstraints = @UniqueConstraint(
               columnNames = {"user_id", "course_id"},
               name = "uk_student_enrollment_user_course"
       ))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "enrolled_at", nullable = false, updatable = false)
    private LocalDateTime enrolledAt;

    /**
     * JPA lifecycle hook: auto-set enrollment timestamp on persist.
     */
    @PrePersist
    private void onPersist() {
        if (this.enrolledAt == null) {
            this.enrolledAt = LocalDateTime.now();
        }
    }
}
