package no.ntnu.idatt2106.nettdetektivene.dto.auth;

public record AuthResponse(
    String token,
    String role,
    Long userId,
    String email
) {}
