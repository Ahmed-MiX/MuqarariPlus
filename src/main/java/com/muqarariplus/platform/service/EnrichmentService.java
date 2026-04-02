package com.muqarariplus.platform.service;

import com.muqarariplus.platform.audit.Auditable;
import com.muqarariplus.platform.dto.EnrichmentSubmissionDTO;
import com.muqarariplus.platform.entity.*;
import com.muqarariplus.platform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════════
 * ENRICHMENT SERVICE — The Expert Vanguard Business Logic Engine
 * Handles expert-submitted enrichments with full validation,
 * entity resolution, and Knowledge Graph association.
 * ═══════════════════════════════════════════════════════════════════
 */
@Service
@RequiredArgsConstructor
public class EnrichmentService {

    private final ExpertRepository expertRepository;
    private final CourseRepository courseRepository;
    private final CourseEnrichmentRepository enrichmentRepository;
    private final ToolRepository toolRepository;
    private final ProfessionalCertificateRepository certificateRepository;
    private final UserRepository userRepository;

    /**
     * Submits a new enrichment from a verified expert.
     *
     * @param expertEmail login identifier (email or username) of the expert
     * @param dto         validated enrichment submission data
     * @throws IllegalArgumentException if expert or course not found
     * @throws IllegalStateException    if expert is not APPROVED
     */
    @Auditable(action = "CREATE", entity = "CourseEnrichment")
    @Transactional
    public void submitNewEnrichment(String expertEmail, EnrichmentSubmissionDTO dto) {

        // ── Step 1: Resolve the Expert entity ──────────────────────────────
        User user = userRepository.findByEmail(expertEmail);
        if (user == null) {
            user = userRepository.findByUsername(expertEmail);
        }
        if (user == null) {
            throw new IllegalArgumentException(
                    "خطأ: لم يتم العثور على المستخدم — Expert user not found for identifier: " + expertEmail);
        }

        Expert expert = expertRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "خطأ: لم يتم العثور على ملف الخبير — No Expert profile found for user: " + expertEmail));

        // ── Step 2: Verify Expert is APPROVED ──────────────────────────────
        if (expert.getStatus() != ExpertStatus.APPROVED) {
            throw new IllegalStateException(
                    "⛔ يجب أن يكون الخبير معتمداً لإرسال إثراء — Only verified (APPROVED) experts may submit enrichments. " +
                    "Current status: " + expert.getStatus());
        }

        // ── Step 3: Resolve the Course ─────────────────────────────────────
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "خطأ: المقرر غير موجود — Course not found with ID: " + dto.getCourseId()));

        // ── Step 4: Build the CourseEnrichment entity ───────────────────────
        CourseEnrichment enrichment = new CourseEnrichment();
        enrichment.setExpert(expert);
        enrichment.setCourse(course);

        // Map pillar fields from DTO
        enrichment.setApplicationAr(dto.getApplicationAr());
        enrichment.setApplicationEn(dto.getApplicationEn());
        enrichment.setRoadmapAr(dto.getRoadmapAr());
        enrichment.setRoadmapEn(dto.getRoadmapEn());

        // Build unified content from pillars (Markdown format)
        String unifiedContent = buildUnifiedContent(dto);
        enrichment.setContent(unifiedContent);

        // ── Step 5: Set status to PENDING ──────────────────────────────────
        enrichment.setStatus(EnrichmentStatus.PENDING);

        // ── Step 6: Set creation timestamp ─────────────────────────────────
        enrichment.setCreatedAt(LocalDateTime.now());

        // ── Step 7: Fetch and set Tools (if IDs provided) ──────────────────
        if (dto.getToolIds() != null && !dto.getToolIds().isEmpty()) {
            List<Tool> tools = toolRepository.findAllById(dto.getToolIds());
            enrichment.setTools(new HashSet<>(tools));
        }

        // ── Step 8: Fetch and set Certificates (if IDs provided) ───────────
        if (dto.getCertificateIds() != null && !dto.getCertificateIds().isEmpty()) {
            List<ProfessionalCertificate> certs = certificateRepository.findAllById(dto.getCertificateIds());
            enrichment.setCertificates(new HashSet<>(certs));
        }

        // ── Step 9: Persist ────────────────────────────────────────────────
        enrichmentRepository.save(enrichment);
    }

    /**
     * Builds a unified Markdown content string from the DTO's pillar fields.
     * This populates the unified `content` column for display.
     */
    private String buildUnifiedContent(EnrichmentSubmissionDTO dto) {
        StringBuilder sb = new StringBuilder();

        sb.append("## Practical Applications / التطبيقات العملية\n\n");
        sb.append("### English\n");
        sb.append(dto.getApplicationEn()).append("\n\n");
        sb.append("### عربي\n");
        sb.append(dto.getApplicationAr()).append("\n\n");

        sb.append("---\n\n");

        sb.append("## Career Roadmap / المسار المهني\n\n");
        sb.append("### English\n");
        sb.append(dto.getRoadmapEn()).append("\n\n");
        sb.append("### عربي\n");
        sb.append(dto.getRoadmapAr()).append("\n");

        return sb.toString();
    }
}
