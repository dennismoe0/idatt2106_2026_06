package no.ntnu.idatt2106.nettdetektivene.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for registering a new teacher account with email and password.
 */
public record RegisterRequest(
    @NotBlank @Email(message = "Invalid email format") String email,
    @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String password
) {}
