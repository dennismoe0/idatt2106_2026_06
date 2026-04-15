package no.ntnu.idatt2106.nettdetektivene.dto.classroom;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinClassroomRequest(
    @NotBlank String code,
    @NotBlank @Size(max = 50) String displayName
) {}
