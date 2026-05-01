package no.ntnu.idatt2106.nettdetektivene.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for simulated student login (Feide-style) using only a username.
 */
public record StudentLoginRequest(
    @NotBlank(message = "Username is required") String username
) {}
