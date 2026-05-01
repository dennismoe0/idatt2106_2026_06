package no.ntnu.idatt2106.nettdetektivene.controller;

import io.github.bucket4j.Bucket;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import no.ntnu.idatt2106.nettdetektivene.config.RateLimitConfig;
import no.ntnu.idatt2106.nettdetektivene.dto.auth.*;
import no.ntnu.idatt2106.nettdetektivene.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * Handles authentication endpoints for teacher registration, teacher/student login, and rate limiting.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final Map<String, Bucket> authRateLimitBuckets;

    /**
     * Registers a new teacher account and returns a JWT token.
     *
     * @param request     the registration details (name, email, password)
     * @param httpRequest the incoming HTTP request used to extract the client IP for rate limiting
     * @return 201 Created with an {@link AuthResponse} containing the JWT token
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new teacher account")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        checkRateLimit(httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    /**
     * Authenticates a teacher with email and password and returns a JWT token.
     *
     * @param request     the login credentials (email and password)
     * @param httpRequest the incoming HTTP request used to extract the client IP for rate limiting
     * @return 200 OK with an {@link AuthResponse} containing the JWT token
     */
    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        checkRateLimit(httpRequest);
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Authenticates a student via simulated Feide login and returns a JWT token.
     *
     * @param request     the student login details (username and classroom code)
     * @param httpRequest the incoming HTTP request used to extract the client IP for rate limiting
     * @return 200 OK with an {@link AuthResponse} containing the JWT token
     */
    @PostMapping("/student-login")
    @Operation(summary = "Student login (simulated Feide)")
    public ResponseEntity<AuthResponse> studentLogin(
            @Valid @RequestBody StudentLoginRequest request,
            HttpServletRequest httpRequest) {
        checkRateLimit(httpRequest);
        return ResponseEntity.ok(authService.studentLogin(request));
    }

    /**
     * Enforces a per-IP rate limit on authentication endpoints.
     *
     * @param request the incoming HTTP request used to extract the client IP
     * @throws org.springframework.web.server.ResponseStatusException 429 Too Many Requests if the rate limit is exceeded
     */
    private void checkRateLimit(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        Bucket bucket = authRateLimitBuckets.computeIfAbsent(ip, k -> RateLimitConfig.newBucket());
        if (!bucket.tryConsume(1)) {
            log.warn("Rate limit exceeded for IP: {}", ip);
            throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.TOO_MANY_REQUESTS, "Too many requests. Try again in a minute.");
        }
    }
}
