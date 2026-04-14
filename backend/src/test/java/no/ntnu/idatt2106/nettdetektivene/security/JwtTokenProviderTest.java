package no.ntnu.idatt2106.nettdetektivene.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider provider;
    private static final String SECRET = "test-secret-that-is-long-enough-for-hmac-sha256-algorithm-yes";

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider(SECRET, 3600000L);
    }

    @Test
    void generateToken_returnsNonEmptyString() {
        String token = provider.generateToken(1L, "TEACHER", "test@test.no");
        assertThat(token).isNotBlank();
    }

    @Test
    void isTokenValid_withValidToken_returnsTrue() {
        String token = provider.generateToken(1L, "TEACHER", "test@test.no");
        assertThat(provider.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_withGarbageToken_returnsFalse() {
        assertThat(provider.isTokenValid("not.a.token")).isFalse();
    }

    @Test
    void extractUserId_returnsCorrectId() {
        String token = provider.generateToken(42L, "STUDENT", "student@test.no");
        assertThat(provider.extractUserId(token)).isEqualTo(42L);
    }

    @Test
    void extractRole_returnsCorrectRole() {
        String token = provider.generateToken(1L, "TEACHER", "teacher@test.no");
        assertThat(provider.extractRole(token)).isEqualTo("TEACHER");
    }
}
