package no.ntnu.idatt2106.nettdetektivene.dto.school;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for a teacher to join an existing school using its invite code.
 */
public record JoinSchoolRequest(
    @NotBlank(message = "School code is required")
    String code
) {}
