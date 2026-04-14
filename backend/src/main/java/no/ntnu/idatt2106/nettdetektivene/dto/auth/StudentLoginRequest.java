package no.ntnu.idatt2106.nettdetektivene.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record StudentLoginRequest(
    @NotBlank(message = "Username is required") String username
) {}
