package no.ntnu.idatt2106.nettdetektivene.dto.classroom;

import jakarta.validation.constraints.NotBlank;

public record CreateClassroomRequest(
    @NotBlank String name,
    String description
) {}
