package com.muqarariplus.platform.payload.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CourseAdditionRequestPayload(
    @NotBlank String courseNameAr,
    @NotBlank String courseNameEn,
    String courseCode,
    String justification,
    @NotNull Long majorId
) {}
