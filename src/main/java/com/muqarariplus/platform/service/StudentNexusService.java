package com.muqarariplus.platform.service;

import com.muqarariplus.platform.audit.Auditable;
import com.muqarariplus.platform.dto.TechCvDTO;
import com.muqarariplus.platform.entity.*;
import com.muqarariplus.platform.repository.CourseEnrichmentRepository;
import com.muqarariplus.platform.repository.CourseRepository;
import com.muqarariplus.platform.repository.StudentEnrollmentRepository;
import com.muqarariplus.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ═══════════════════════════════════════════════════════════════════
 * STUDENT NEXUS SERVICE — The Knowledge Graph Aggregation Engine
 * Handles student course enrollment via the StudentEnrollment entity
 * and generates a dynamic TechCV by extracting all Tools and
 * ProfessionalCertificates from APPROVED CourseEnrichments across
 * the student's enrolled courses.
 * ═══════════════════════════════════════════════════════════════════
 */
@Service
@RequiredArgsConstructor
public class StudentNexusService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final StudentEnrollmentRepository enrollmentRepository;
    private final CourseEnrichmentRepository enrichmentRepository;

    // ═══════════════════════════════════════════════════════════════
    // METHOD 1: ENROLL — Register a student in a course
    // ═══════════════════════════════════════════════════════════════

    /**
     * Enrolls a student in a course. Creates a StudentEnrollment record
     * AND adds the course to User.enrolledCourses @ManyToMany for
     * backward compatibility with existing dashboard logic.
     *
     * @param email    student's email (login identifier)
     * @param courseId the course to enroll in
     * @throws IllegalArgumentException if user or course not found
     * @throws IllegalStateException    if already enrolled
     */
    @Auditable(action = "ENROLL", entity = "StudentEnrollment")
    @Transactional
    public void enroll(String email, Long courseId) {

        // ── Step 1: Resolve User ───────────────────────────────────────
        User user = resolveUser(email);

        // ── Step 2: Resolve Course ─────────────────────────────────────
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "خطأ: المقرر غير موجود — Course not found with ID: " + courseId));

        // ── Step 3: Check for duplicate enrollment ─────────────────────
        if (enrollmentRepository.existsByUserEmailAndCourseId(email, courseId)) {
            throw new IllegalStateException(
                    "⚠️ أنت مسجل بالفعل في هذا المقرر — You are already enrolled in this course.");
        }

        // ── Step 4: Create StudentEnrollment record ────────────────────
        StudentEnrollment enrollment = new StudentEnrollment();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollmentRepository.save(enrollment);

        // ── Step 5: Sync with User.enrolledCourses @ManyToMany ─────────
        // This ensures backward compatibility with StudentDashboardService
        user.addCourse(course);
        userRepository.save(user);
    }

    // ═══════════════════════════════════════════════════════════════
    // METHOD 2: GENERATE TECH CV — Aggregate Knowledge Graph
    // ═══════════════════════════════════════════════════════════════

    /**
     * Generates a TechCV by:
     * 1. Finding the student User by email.
     * 2. Fetching all StudentEnrollments for this user.
     * 3. Extracting a List of enrolled Courses.
     * 4. For EACH course, fetching ALL APPROVED CourseEnrichments.
     * 5. FlatMapping all Tools into a distinct Set.
     * 6. FlatMapping all ProfessionalCertificates into a distinct Set.
     * 7. Building and returning the TechCvDTO.
     *
     * @param email student's email (login identifier)
     * @return fully-populated TechCvDTO
     */
    @Transactional(readOnly = true)
    public TechCvDTO generateTechCv(String email) {

        // ── Step 1: Find User by email ─────────────────────────────────
        User user = resolveUser(email);

        // ── Step 2: Fetch all StudentEnrollments for this user ─────────
        List<StudentEnrollment> enrollments = enrollmentRepository.findByUserEmail(email);

        // ── Step 3: Extract List<Course> from enrollments ──────────────
        List<Course> enrolledCourses = enrollments.stream()
                .map(StudentEnrollment::getCourse)
                .collect(Collectors.toList());

        // ── Step 4: For each course, fetch APPROVED CourseEnrichments ──
        List<CourseEnrichment> allApprovedEnrichments = enrolledCourses.stream()
                .map(course -> enrichmentRepository.findByCourseIdAndStatus(
                        course.getId(), EnrichmentStatus.APPROVED))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        // ── Step 5: FlatMap all Tools into a distinct Set ──────────────
        // Handles potential null collections inside CourseEnrichment
        Set<Tool> extractedTools = allApprovedEnrichments.stream()
                .map(CourseEnrichment::getTools)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // ── Step 6: FlatMap all ProfessionalCertificates into a Set ────
        // Handles potential null collections inside CourseEnrichment
        Set<ProfessionalCertificate> extractedCertificates = allApprovedEnrichments.stream()
                .map(CourseEnrichment::getCertificates)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // ── Step 7: Build and return the TechCvDTO ─────────────────────
        String studentName = buildDisplayName(user);

        return TechCvDTO.builder()
                .studentName(studentName)
                .email(user.getEmail())
                .enrolledCourses(enrolledCourses)
                .extractedTools(extractedTools)
                .extractedCertificates(extractedCertificates)
                .build();
    }

    // ═══════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Resolves User from email or username.
     */
    private User resolveUser(String identifier) {
        User user = userRepository.findByEmail(identifier);
        if (user == null) {
            user = userRepository.findByUsername(identifier);
        }
        if (user == null) {
            throw new IllegalArgumentException(
                    "خطأ: لم يتم العثور على المستخدم — User not found: " + identifier);
        }
        return user;
    }

    /**
     * Builds a display name from User firstName + lastName.
     * Falls back to email prefix if names are not set.
     */
    private String buildDisplayName(User user) {
        String firstName = user.getFirstName();
        String lastName = user.getLastName();

        if (firstName != null && !firstName.isBlank() && lastName != null && !lastName.isBlank()) {
            return firstName.trim() + " " + lastName.trim();
        } else if (firstName != null && !firstName.isBlank()) {
            return firstName.trim();
        } else {
            // Fallback: use email prefix
            return user.getEmail().split("@")[0];
        }
    }
}
