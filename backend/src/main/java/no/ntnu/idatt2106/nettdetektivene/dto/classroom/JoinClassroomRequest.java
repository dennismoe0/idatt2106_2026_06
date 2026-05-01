package no.ntnu.idatt2106.nettdetektivene.dto.classroom;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for a student to join a classroom using an invite code and a chosen display name.
 */
public record JoinClassroomRequest(
    @NotBlank String code,
    @NotBlank @Size(max = 50) String displayName
) {}
