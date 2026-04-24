package no.ntnu.idatt2106.nettdetektivene.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WeeklyMysterySubmissionDto(
    @NotBlank @Size(max = 200) String title,
    @Size(max = 2000) String description,
    String imageUrl,
    @NotNull Long classroomId
) {}
