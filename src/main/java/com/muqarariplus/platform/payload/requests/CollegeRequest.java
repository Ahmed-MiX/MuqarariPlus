package com.muqarariplus.platform.payload.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CollegeRequest(
    @NotNull Long universityId,
    @NotBlank String nameEn,
    @NotBlank String nameAr
) {}
