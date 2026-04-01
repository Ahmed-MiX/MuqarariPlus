package com.muqarariplus.platform.service;

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

    public CourseEnrichmentService(CourseEnrichmentRepository enrichmentRepository,
                                  CourseRepository courseRepository,
                                  ExpertRepository expertRepository,
                                  UserRepository userRepository,
                                  SkillRepository skillRepository,
                                  ToolRepository toolRepository) {
        this.enrichmentRepository = enrichmentRepository;
        this.courseRepository = courseRepository;
        this.expertRepository = expertRepository;
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.toolRepository = toolRepository;
    }

    /**
     * Creates a new course enrichment submitted by a verified expert.
     *
     * @param loginIdentifier The expert's login identifier (email or username from Principal)
     * @param courseId        The target course ID
     * @param content         The rich-text / markdown content
     * @param skillIds        List of Skill IDs to tag
     * @param toolIds         List of Tool IDs to tag
     */
    @Transactional
    public void createEnrichment(String loginIdentifier, Long courseId, String content,
                                 List<Long> skillIds, List<Long> toolIds) {

        // 1. Resolve the User — supports both email and username login
        User user = userRepository.findByEmail(loginIdentifier);
        if (user == null) {
            user = userRepository.findByUsername(loginIdentifier);
        }
        if (user == null) {
            throw new IllegalArgumentException("User not found for identifier: " + loginIdentifier);
        }

        // 2. Fetch the Expert entity linked to this user
        Expert expert = expertRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No Expert profile found for user: " + loginIdentifier));

        // 3. Verify the expert is APPROVED
        if (expert.getStatus() != ExpertStatus.APPROVED) {
            throw new IllegalStateException("Only verified experts may submit enrichments.");
        }

        // 4. Fetch the target Course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Course not found with ID: " + courseId));

        // 5. Fetch tagged Skills and Tools
        var skills = skillRepository.findAllById(skillIds != null ? skillIds : List.of());
        var tools = toolRepository.findAllById(toolIds != null ? toolIds : List.of());

        // 6. Build and persist the enrichment
        CourseEnrichment enrichment = new CourseEnrichment();
        enrichment.setExpert(expert);
        enrichment.setCourse(course);
        enrichment.setContent(content);
        enrichment.setSkills(new HashSet<>(skills));
        enrichment.setTools(new HashSet<>(tools));
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
     * Returns all enrichments with PENDING status for the admin moderation queue.
     */
    public List<CourseEnrichment> getPendingEnrichments() {
        return enrichmentRepository.findByStatus(EnrichmentStatus.PENDING);
    }

    /**
     * Admin approves an enrichment — sets status to APPROVED.
     */
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
    @Transactional
    public void rejectEnrichment(Long enrichmentId) {
        CourseEnrichment enrichment = enrichmentRepository.findById(enrichmentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Enrichment not found with ID: " + enrichmentId));
        enrichment.setStatus(EnrichmentStatus.REJECTED);
        enrichmentRepository.save(enrichment);
    }
}
