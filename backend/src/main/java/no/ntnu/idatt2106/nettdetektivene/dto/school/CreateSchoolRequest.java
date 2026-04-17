package no.ntnu.idatt2106.nettdetektivene.dto.school;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSchoolRequest(
    @NotBlank(message = "School name is required")
    @Size(max = 200, message = "School name must be at most 200 characters")
    String name
) {}
