package com.muqarariplus.platform.service;

import com.muqarariplus.platform.audit.Auditable;
import com.muqarariplus.platform.entity.CourseEnrichment;
import com.muqarariplus.platform.entity.EnrichmentStatus;
import com.muqarariplus.platform.repository.CourseEnrichmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════════
 * MODERATION SERVICE — The Bridge Content Integrity Engine
 * Handles the administrative review lifecycle for CourseEnrichments
 * submitted by experts. Provides PENDING queue retrieval and
 * atomic APPROVE/REJECT state transitions with full audit tracing.
 * ═══════════════════════════════════════════════════════════════════
 */
@Service
@RequiredArgsConstructor
public class ModerationService {

    private final CourseEnrichmentRepository enrichmentRepository;

    // ═══════════════════════════════════════════════════════════════
    // METHOD 1: GET PENDING ENRICHMENTS — The Moderation Queue
    // Returns all CourseEnrichments awaiting admin review.
    // ═══════════════════════════════════════════════════════════════

    /**
     * Fetches all CourseEnrichments with status = PENDING.
     * Used by the Moderation Dashboard to display the review queue.
     *
     * @return list of pending enrichments, never null
     */
    @Transactional(readOnly = true)
    public List<CourseEnrichment> getPendingEnrichments() {
        return enrichmentRepository.findByStatus(EnrichmentStatus.PENDING);
    }

    // ═══════════════════════════════════════════════════════════════
    // METHOD 2: APPROVE ENRICHMENT — Green Light Protocol
    // Transitions enrichment status from PENDING to APPROVED.
    // The enrichment becomes visible to students immediately.
    // ═══════════════════════════════════════════════════════════════

    /**
     * Approves a CourseEnrichment by ID.
     * Sets verificationStatus to APPROVED and persists the change.
     *
     * @param id the CourseEnrichment ID to approve
     * @throws IllegalArgumentException if enrichment not found
     */
    @Auditable(action = "APPROVE", entity = "CourseEnrichment")
    @Transactional
    public void approveEnrichment(Long id) {
        CourseEnrichment enrichment = enrichmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "خطأ: الإثراء غير موجود — CourseEnrichment not found with ID: " + id));

        enrichment.setStatus(EnrichmentStatus.APPROVED);
        enrichmentRepository.save(enrichment);
    }

    // ═══════════════════════════════════════════════════════════════
    // METHOD 3: REJECT ENRICHMENT — Red Flag Protocol
    // Transitions enrichment status from PENDING to REJECTED.
    // The enrichment is permanently hidden from student views.
    // ═══════════════════════════════════════════════════════════════

    /**
     * Rejects a CourseEnrichment by ID.
     * Sets verificationStatus to REJECTED and persists the change.
     *
     * @param id the CourseEnrichment ID to reject
     * @throws IllegalArgumentException if enrichment not found
     */
    @Auditable(action = "REJECT", entity = "CourseEnrichment")
    @Transactional
    public void rejectEnrichment(Long id) {
        CourseEnrichment enrichment = enrichmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "خطأ: الإثراء غير موجود — CourseEnrichment not found with ID: " + id));

        enrichment.setStatus(EnrichmentStatus.REJECTED);
        enrichmentRepository.save(enrichment);
    }
}
