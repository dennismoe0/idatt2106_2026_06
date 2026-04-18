package no.ntnu.idatt2106.nettdetektivene.dto.classroom;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateDisplayNameRequest(
    @NotBlank @Size(min = 2, max = 50) String displayName
) {}
