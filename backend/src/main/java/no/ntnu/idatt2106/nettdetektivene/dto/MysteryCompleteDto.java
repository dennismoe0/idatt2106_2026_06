package no.ntnu.idatt2106.nettdetektivene.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for a student submitting their answer to the weekly mystery challenge.
 */
public record MysteryCompleteDto(
    @NotNull Long classroomId,
    @NotBlank String answer
) {}
