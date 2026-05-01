package no.ntnu.idatt2106.nettdetektivene.dto.classroom;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for a student to update their display name within a classroom.
 */
public record UpdateDisplayNameRequest(
    @NotBlank @Size(min = 2, max = 50) String displayName
) {}
