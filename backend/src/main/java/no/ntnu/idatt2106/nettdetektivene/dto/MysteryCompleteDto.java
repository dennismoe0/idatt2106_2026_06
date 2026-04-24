package no.ntnu.idatt2106.nettdetektivene.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MysteryCompleteDto(
    @NotNull Long classroomId,
    @NotBlank String answer
) {}
