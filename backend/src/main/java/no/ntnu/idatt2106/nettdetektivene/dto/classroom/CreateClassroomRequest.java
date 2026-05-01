package no.ntnu.idatt2106.nettdetektivene.dto.classroom;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for a teacher to create a new classroom with a name and optional description.
 */
public record CreateClassroomRequest(
    @NotBlank @Size(max = 100) String name,
    String description
) {}
