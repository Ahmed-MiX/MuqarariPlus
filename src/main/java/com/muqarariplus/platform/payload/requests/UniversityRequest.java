package com.muqarariplus.platform.payload.requests;

import jakarta.validation.constraints.NotBlank;

public record UniversityRequest(
    @NotBlank String nameEn,
    @NotBlank String nameAr
) {}
