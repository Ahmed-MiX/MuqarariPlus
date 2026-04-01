package com.muqarariplus.platform.payload.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MajorRequest(
    @NotNull Long collegeId,
    @NotBlank String nameEn,
    @NotBlank String nameAr,
    String code
) {}
