package no.ntnu.idatt2106.nettdetektivene.dto.auth;

/**
 * Response payload returned after a successful authentication, carrying the JWT and basic user identity.
 */
public record AuthResponse(
    String token,
    String role,
    Long userId,
    String email
) {}
