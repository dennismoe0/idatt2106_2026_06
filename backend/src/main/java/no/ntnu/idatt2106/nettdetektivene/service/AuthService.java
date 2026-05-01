package no.ntnu.idatt2106.nettdetektivene.service;

import lombok.RequiredArgsConstructor;
import no.ntnu.idatt2106.nettdetektivene.dto.auth.*;
import no.ntnu.idatt2106.nettdetektivene.entity.User;
import no.ntnu.idatt2106.nettdetektivene.exception.AuthException;
import no.ntnu.idatt2106.nettdetektivene.repository.UserRepository;
import no.ntnu.idatt2106.nettdetektivene.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.UUID;

/**
 * Handles authentication for teachers and students, including registration and JWT issuance.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Registers a new teacher account.
     *
     * @param request the registration details (email and password)
     * @return an {@link AuthResponse} containing a JWT and user metadata
     * @throws AuthException if the email is already in use
     */
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new teacher: {}", request.email());
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Registration failed — email already in use: {}", request.email());
            throw new AuthException("Email already in use");
        }
        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(User.Role.TEACHER);
        user = userRepository.save(user);
        log.info("Teacher registered successfully: id={} email={}", user.getId(), user.getEmail());
        return toAuthResponse(user);
    }

    /**
     * Authenticates a teacher by email and password.
     *
     * @param request the login credentials
     * @return an {@link AuthResponse} containing a JWT and user metadata
     * @throws AuthException if the credentials are invalid
     */
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> {
                log.warn("Login attempt for unknown email: {}", request.email());
                return new AuthException("Invalid credentials");
            });
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Failed login for email: {}", request.email());
            throw new AuthException("Invalid credentials");
        }
        log.info("Login successful: id={} role={}", user.getId(), user.getRole());
        return toAuthResponse(user);
    }

    /**
     * Performs a simulated Feide login for students, creating a new account if one does not exist.
     *
     * @param request the student login details (username)
     * @return an {@link AuthResponse} containing a JWT and user metadata
     */
    public AuthResponse studentLogin(StudentLoginRequest request) {
        log.info("Student login attempt: username={}", request.username());
        String email = request.username() + "@student.local";
        boolean[] created = { false };
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
            newUser.setRole(User.Role.STUDENT);
            created[0] = true;
            return userRepository.save(newUser);
        });
        log.info("Student {} — id={} email={}", created[0] ? "created" : "found", user.getId(), email);
        return toAuthResponse(user);
    }

    private AuthResponse toAuthResponse(User user) {
        String token = jwtTokenProvider.generateToken(user.getId(), user.getRole().name(), user.getEmail());
        return new AuthResponse(token, user.getRole().name(), user.getId(), user.getEmail());
    }
}
