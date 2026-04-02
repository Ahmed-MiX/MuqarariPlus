package com.muqarariplus.platform.service;

import com.muqarariplus.platform.audit.Auditable;
import com.muqarariplus.platform.entity.*;
import com.muqarariplus.platform.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
public class CourseEnrichmentService {

    private final CourseEnrichmentRepository enrichmentRepository;
    private final CourseRepository courseRepository;
    private final ExpertRepository expertRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final ToolRepository toolRepository;
    private final ProfessionalCertificateRepository certRepository;

    public CourseEnrichmentService(CourseEnrichmentRepository enrichmentRepository,
                                  CourseRepository courseRepository,
                                  ExpertRepository expertRepository,
                                  UserRepository userRepository,
                                  SkillRepository skillRepository,
                                  ToolRepository toolRepository,
                                  ProfessionalCertificateRepository certRepository) {
        this.enrichmentRepository = enrichmentRepository;
        this.courseRepository = courseRepository;
        this.expertRepository = expertRepository;
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.toolRepository = toolRepository;
        this.certRepository = certRepository;
    }

    /**
     * Creates a new course enrichment submitted by a verified expert.
     */
    @Auditable(action = "CREATE", entity = "CourseEnrichment")
    @Transactional
    public void createEnrichment(String loginIdentifier, Long courseId, String content,
                                 List<Long> skillIds, List<Long> toolIds, List<Long> certIds) {

        User user = userRepository.findByEmail(loginIdentifier);
        if (user == null) {
            user = userRepository.findByUsername(loginIdentifier);
        }
        if (user == null) {
            throw new IllegalArgumentException("User not found for identifier: " + loginIdentifier);
        }

        Expert expert = expertRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No Expert profile found for user: " + loginIdentifier));

        if (expert.getStatus() != ExpertStatus.APPROVED) {
            throw new IllegalStateException("Only verified experts may submit enrichments.");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Course not found with ID: " + courseId));

        var skills = skillRepository.findAllById(skillIds != null ? skillIds : List.of());
        var tools = toolRepository.findAllById(toolIds != null ? toolIds : List.of());
        var certs = certRepository.findAllById(certIds != null ? certIds : List.of());

        CourseEnrichment enrichment = new CourseEnrichment();
        enrichment.setExpert(expert);
        enrichment.setCourse(course);
        enrichment.setContent(content);
        enrichment.setSkills(new HashSet<>(skills));
        enrichment.setTools(new HashSet<>(tools));
        enrichment.setCertificates(new HashSet<>(certs));
        enrichment.setStatus(EnrichmentStatus.PENDING);

        enrichmentRepository.save(enrichment);
    }

    /**
     * Returns all enrichments submitted by a specific expert (via User ID).
     */
    public List<CourseEnrichment> getEnrichmentsByExpertUserId(Long userId) {
        return enrichmentRepository.findByExpertUserId(userId);
    }

    /**
     * Returns all enrichments with a given status (for admin queue).
     */
    public List<CourseEnrichment> getEnrichmentsByStatus(EnrichmentStatus status) {
        return enrichmentRepository.findByStatus(status);
    }

    /**
     * Returns approved enrichments for a specific course (for student view).
     */
    public List<CourseEnrichment> getApprovedEnrichmentsForCourse(Long courseId) {
        return enrichmentRepository.findByCourseIdAndStatus(courseId, EnrichmentStatus.APPROVED);
    }

    /**
     * Returns the count of APPROVED enrichments for a course (for catalog badge).
     */
    public long getApprovedCountForCourse(Long courseId) {
        return enrichmentRepository.countByCourseIdAndStatus(courseId, EnrichmentStatus.APPROVED);
    }

    /**
     * Returns all enrichments with PENDING status for the admin moderation queue.
     */
    public List<CourseEnrichment> getPendingEnrichments() {
        return enrichmentRepository.findByStatus(EnrichmentStatus.PENDING);
    }

    /**
     * Admin approves an enrichment — sets status to APPROVED.
     */
    @Auditable(action = "APPROVE", entity = "CourseEnrichment")
    @Transactional
    public void approveEnrichment(Long enrichmentId) {
        CourseEnrichment enrichment = enrichmentRepository.findById(enrichmentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Enrichment not found with ID: " + enrichmentId));
        enrichment.setStatus(EnrichmentStatus.APPROVED);
        enrichmentRepository.save(enrichment);
    }

    /**
     * Admin rejects an enrichment — sets status to REJECTED.
     */
    @Auditable(action = "REJECT", entity = "CourseEnrichment")
    @Transactional
    public void rejectEnrichment(Long enrichmentId) {
        CourseEnrichment enrichment = enrichmentRepository.findById(enrichmentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Enrichment not found with ID: " + enrichmentId));
        enrichment.setStatus(EnrichmentStatus.REJECTED);
        enrichmentRepository.save(enrichment);
    }

    // ── Expert Impact Metrics ───────────────────────────────────────────

    /**
     * Count of APPROVED enrichments by an expert (identified by login identifier).
     */
    public long countApprovedEnrichmentsByExpert(String identifier) {
        User user = resolveUser(identifier);
        return enrichmentRepository.findByExpertUserId(user.getId()).stream()
                .filter(e -> e.getStatus() == EnrichmentStatus.APPROVED)
                .count();
    }

    /**
     * Count of PENDING enrichments by an expert.
     */
    public long countPendingEnrichmentsByExpert(String identifier) {
        User user = resolveUser(identifier);
        return enrichmentRepository.findByExpertUserId(user.getId()).stream()
                .filter(e -> e.getStatus() == EnrichmentStatus.PENDING)
                .count();
    }

    /**
     * THE IMPACT ENGINE: Calculates total students impacted by this expert.
     * Finds all courses in the expert's APPROVED enrichments, then sums up
     * students enrolled in those courses across the platform.
     */
    @Transactional(readOnly = true)
    public long calculateTotalStudentImpact(String identifier) {
        User user = resolveUser(identifier);
        List<CourseEnrichment> approved = enrichmentRepository.findByExpertUserId(user.getId()).stream()
                .filter(e -> e.getStatus() == EnrichmentStatus.APPROVED)
                .toList();

        // Collect unique course IDs that this expert has enriched
        var enrichedCourseIds = approved.stream()
                .map(e -> e.getCourse().getId())
                .collect(java.util.stream.Collectors.toSet());

        // Count students enrolled in those specific courses
        return userRepository.findByRole("ROLE_STUDENT").stream()
                .filter(student -> student.getEnrolledCourses().stream()
                        .anyMatch(c -> enrichedCourseIds.contains(c.getId())))
                .count();
    }

    /**
     * Returns all enrichments by an expert (all statuses).
     */
    public List<CourseEnrichment> getAllEnrichmentsByExpert(String identifier) {
        User user = resolveUser(identifier);
        return enrichmentRepository.findByExpertUserId(user.getId());
    }

    /**
     * Resolves User from login identifier (email or username).
     */
    private User resolveUser(String identifier) {
        User user = userRepository.findByEmail(identifier);
        if (user == null) {
            user = userRepository.findByUsername(identifier);
        }
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + identifier);
        }
        return user;
    }
}

