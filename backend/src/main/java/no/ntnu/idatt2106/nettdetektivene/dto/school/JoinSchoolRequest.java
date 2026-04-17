package no.ntnu.idatt2106.nettdetektivene.dto.school;

import jakarta.validation.constraints.NotBlank;

public record JoinSchoolRequest(
    @NotBlank(message = "School code is required")
    String code
) {}
