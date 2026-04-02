package com.muqarariplus.platform.payload.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CourseRequest(
    @NotBlank String code,
    @NotBlank String nameEn,
    @NotBlank String nameAr,
    String descriptionEn,
    String descriptionAr,
    String syllabusUrl,
    @NotNull Long majorId
) {}
