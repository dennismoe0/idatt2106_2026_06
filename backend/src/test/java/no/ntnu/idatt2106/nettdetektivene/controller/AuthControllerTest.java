package no.ntnu.idatt2106.nettdetektivene.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bucket;
import no.ntnu.idatt2106.nettdetektivene.config.RateLimitConfig;
import no.ntnu.idatt2106.nettdetektivene.dto.auth.AuthResponse;
import no.ntnu.idatt2106.nettdetektivene.exception.AuthException;
import no.ntnu.idatt2106.nettdetektivene.security.JwtAuthFilter;
import no.ntnu.idatt2106.nettdetektivene.security.JwtTokenProvider;
import no.ntnu.idatt2106.nettdetektivene.security.UserDetailsServiceImpl;
import no.ntnu.idatt2106.nettdetektivene.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
        }

        @Bean
        FilterRegistrationBean<JwtAuthFilter> jwtFilterRegistration(JwtAuthFilter jwtAuthFilter) {
            FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>(jwtAuthFilter);
            registration.setEnabled(false);
            return registration;
        }
    }

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean AuthService authService;
    @MockBean JwtAuthFilter jwtAuthFilter;
    @MockBean JwtTokenProvider jwtTokenProvider;
    @MockBean UserDetailsServiceImpl userDetailsService;
    @MockBean(name = "authRateLimitBuckets")
    Map<String, Bucket> authRateLimitBuckets;

    @BeforeEach
    void setup() {
        Bucket realBucket = RateLimitConfig.newBucket();
        when(authRateLimitBuckets.computeIfAbsent(any(), any())).thenReturn(realBucket);
    }

    @Test
    void login_validCredentials_returns200WithToken() throws Exception {
        when(authService.login(any()))
            .thenReturn(new AuthResponse("test-token", "TEACHER", 1L, "teacher@test.no"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "email": "teacher@test.no", "password": "password123" }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("test-token"))
            .andExpect(jsonPath("$.role").value("TEACHER"))
            .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void login_invalidCredentials_returns401() throws Exception {
        when(authService.login(any())).thenThrow(new AuthException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "email": "teacher@test.no", "password": "wrong" }
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    void login_missingEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "password": "password123" }
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void register_newTeacher_returns201() throws Exception {
        when(authService.register(any()))
            .thenReturn(new AuthResponse("new-token", "TEACHER", 2L, "new@test.no"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "email": "new@test.no", "password": "password123" }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").value("new-token"));
    }

    @Test
    void studentLogin_validUsername_returns200() throws Exception {
        when(authService.studentLogin(any()))
            .thenReturn(new AuthResponse("student-token", "STUDENT", 3L, "student1@student.local"));

        mockMvc.perform(post("/api/auth/student-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "username": "student1" }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.role").value("STUDENT"));
    }
}
