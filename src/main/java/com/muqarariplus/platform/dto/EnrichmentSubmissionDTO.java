package com.muqarariplus.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════════
 * ENRICHMENT SUBMISSION DTO — The Expert Vanguard Data Contract
 * Carries all expert-submitted enrichment data from the Thymeleaf
 * form to the service layer with full Jakarta Bean Validation.
 * ═══════════════════════════════════════════════════════════════════
 */
@Data
public class EnrichmentSubmissionDTO {

    @NotNull(message = "يجب اختيار مقرر دراسي — Course selection is required.")
    private Long courseId;

    @NotBlank(message = "حقل التطبيقات العملية (عربي) مطلوب — Arabic practical applications field is required.")
    private String applicationAr;

    @NotBlank(message = "حقل التطبيقات العملية (إنجليزي) مطلوب — English practical applications field is required.")
    private String applicationEn;

    @NotBlank(message = "حقل المسار المهني (عربي) مطلوب — Arabic career roadmap field is required.")
    private String roadmapAr;

    @NotBlank(message = "حقل المسار المهني (إنجليزي) مطلوب — English career roadmap field is required.")
    private String roadmapEn;

    private List<Long> toolIds;

    private List<Long> certificateIds;
}
