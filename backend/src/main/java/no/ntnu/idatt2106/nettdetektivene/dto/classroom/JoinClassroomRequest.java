package no.ntnu.idatt2106.nettdetektivene.dto.classroom;

import jakarta.validation.constraints.NotBlank;

public record JoinClassroomRequest(
    @NotBlank String code,
    @NotBlank String displayName
) {}
