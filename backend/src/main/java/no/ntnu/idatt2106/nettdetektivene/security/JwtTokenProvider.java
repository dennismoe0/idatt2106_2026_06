package no.ntnu.idatt2106.nettdetektivene.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Issues and validates HMAC-signed JWTs used for stateless authentication.
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final String jwtSecret;
    private final long expirationMs;

    public JwtTokenProvider(
        @Value("${app.jwt.secret}") String jwtSecret,
        @Value("${app.jwt.expiration-ms}") long expirationMs
    ) {
        this.jwtSecret = jwtSecret;
        this.expirationMs = expirationMs;
    }

    /**
     * Generates a signed JWT containing the user ID (subject), role, and email claims.
     *
     * @param userId the user's database ID
     * @param role   the user's role name (e.g. {@code "TEACHER"} or {@code "STUDENT"})
     * @param email  the user's email address
     * @return the compact JWT string
     */
    public String generateToken(Long userId, String role, String email) {
        log.debug("Generating JWT for userId={} role={}", userId, role);
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("role", role)
            .claim("email", email)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expirationMs))
            .signWith(getSigningKey())
            .compact();
    }

    /**
     * Returns {@code true} if the token is well-formed, signed correctly, and not expired.
     *
     * @param token the JWT string to validate
     * @return {@code true} if valid, {@code false} otherwise
     */
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extracts the user ID from the JWT subject claim.
     *
     * @param token the JWT string
     * @return the user's database ID
     */
    public Long extractUserId(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    /**
     * Extracts the role claim from the JWT.
     *
     * @param token the JWT string
     * @return the role string (e.g. {@code "TEACHER"} or {@code "STUDENT"})
     */
    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
