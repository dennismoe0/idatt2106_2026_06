package no.ntnu.idatt2106.nettdetektivene.service;

import no.ntnu.idatt2106.nettdetektivene.dto.auth.*;
import no.ntnu.idatt2106.nettdetektivene.entity.User;
import no.ntnu.idatt2106.nettdetektivene.exception.AuthException;
import no.ntnu.idatt2106.nettdetektivene.repository.UserRepository;
import no.ntnu.idatt2106.nettdetektivene.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtTokenProvider jwtTokenProvider;
    @InjectMocks AuthService authService;

    @Test
    void login_validCredentials_returnsAuthResponse() {
        User user = new User();
        user.setId(1L);
        user.setEmail("teacher@test.no");
        user.setPasswordHash("hashed");
        user.setRole(User.Role.TEACHER);

        when(userRepository.findByEmail("teacher@test.no")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashed")).thenReturn(true);
        when(jwtTokenProvider.generateToken(1L, "TEACHER", "teacher@test.no")).thenReturn("jwt-token");

        AuthResponse response = authService.login(new LoginRequest("teacher@test.no", "password"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.role()).isEqualTo("TEACHER");
        assertThat(response.userId()).isEqualTo(1L);
    }

    @Test
    void login_wrongPassword_throwsAuthException() {
        User user = new User();
        user.setEmail("teacher@test.no");
        user.setPasswordHash("hashed");
        user.setRole(User.Role.TEACHER);

        when(userRepository.findByEmail("teacher@test.no")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("teacher@test.no", "wrong")))
            .isInstanceOf(AuthException.class)
            .hasMessage("Invalid credentials");
    }

    @Test
    void login_unknownEmail_throwsAuthExceptionWithSameMessage() {
        when(userRepository.findByEmail("unknown@test.no")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("unknown@test.no", "password")))
            .isInstanceOf(AuthException.class)
            .hasMessage("Invalid credentials");
    }

    @Test
    void register_newEmail_returnsAuthResponse() {
        when(userRepository.existsByEmail("new@test.no")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        User saved = new User();
        saved.setId(5L);
        saved.setEmail("new@test.no");
        saved.setPasswordHash("hashed");
        saved.setRole(User.Role.TEACHER);
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(jwtTokenProvider.generateToken(5L, "TEACHER", "new@test.no")).thenReturn("new-token");

        AuthResponse response = authService.register(new RegisterRequest("new@test.no", "password123"));

        assertThat(response.token()).isEqualTo("new-token");
        assertThat(response.role()).isEqualTo("TEACHER");
    }

    @Test
    void register_duplicateEmail_throwsAuthException() {
        when(userRepository.existsByEmail("taken@test.no")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("taken@test.no", "password123")))
            .isInstanceOf(AuthException.class)
            .hasMessage("Email already in use");
    }
}
