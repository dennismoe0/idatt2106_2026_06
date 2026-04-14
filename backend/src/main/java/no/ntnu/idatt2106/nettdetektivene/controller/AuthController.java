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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final Map<String, Bucket> authRateLimitBuckets;

    @PostMapping("/register")
    @Operation(summary = "Register a new teacher account")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        checkRateLimit(httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        checkRateLimit(httpRequest);
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/student-login")
    @Operation(summary = "Student login (simulated Feide)")
    public ResponseEntity<AuthResponse> studentLogin(
            @Valid @RequestBody StudentLoginRequest request,
            HttpServletRequest httpRequest) {
        checkRateLimit(httpRequest);
        return ResponseEntity.ok(authService.studentLogin(request));
    }

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
