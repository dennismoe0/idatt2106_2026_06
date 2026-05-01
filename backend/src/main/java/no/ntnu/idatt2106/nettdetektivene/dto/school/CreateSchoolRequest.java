package no.ntnu.idatt2106.nettdetektivene.dto.school;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating a new school entity with a unique name.
 */
public record CreateSchoolRequest(
    @NotBlank(message = "School name is required")
    @Size(max = 200, message = "School name must be at most 200 characters")
    String name
) {}
