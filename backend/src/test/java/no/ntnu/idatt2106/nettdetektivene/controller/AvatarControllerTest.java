package no.ntnu.idatt2106.nettdetektivene.controller;

import no.ntnu.idatt2106.nettdetektivene.dto.avatar.AvatarOptionsResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.avatar.AvatarResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.avatar.MedalLockedItem;
import no.ntnu.idatt2106.nettdetektivene.security.JwtAuthFilter;
import no.ntnu.idatt2106.nettdetektivene.security.JwtTokenProvider;
import no.ntnu.idatt2106.nettdetektivene.security.UserDetailsServiceImpl;
import no.ntnu.idatt2106.nettdetektivene.service.AvatarService;
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

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AvatarController.class)
class AvatarControllerTest {

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

    @Autowired
    MockMvc mockMvc;

    @MockBean
    AvatarService avatarService;

    @MockBean
    JwtAuthFilter jwtAuthFilter;

    @MockBean
    JwtTokenProvider jwtTokenProvider;

    @MockBean
    UserDetailsServiceImpl userDetailsService;

    @Test
    void getMyAvatar_returns200WithAvatar() throws Exception {
        when(avatarService.getMyAvatar()).thenReturn(new AvatarResponse(
            "neutral",
            "#4a3000",
            "round",
            "#D08B5B",
            "#8B4513",
            "short",
            "detective-coat",
            "#2563eb",
            "none",
            "badge"
        ));

        mockMvc.perform(get("/api/avatars/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.gender").value("neutral"))
            .andExpect(jsonPath("$.outfit").value("detective-coat"))
            .andExpect(jsonPath("$.accessory").value("badge"));
    }

    @Test
    void updateMyAvatar_validRequest_returns200WithUpdatedAvatar() throws Exception {
        when(avatarService.updateMyAvatar(any())).thenReturn(new AvatarResponse(
            "female",
            "#15803d",
            "round",
            "#D08B5B",
            "#1a1a1a",
            "curly",
            "hoodie",
            "#2563eb",
            "none",
            "badge"
        ));

        mockMvc.perform(put("/api/avatars/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "gender": "female",
                      "eyeColor": "#15803d",
                      "eyeStyle": "round",
                      "skinColor": "#D08B5B",
                      "hairColor": "#1a1a1a",
                      "hairStyle": "curly",
                      "outfit": "hoodie",
                      "outfitColor": "#2563eb",
                      "hatColor": "none",
                      "accessory": "badge"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.gender").value("female"))
            .andExpect(jsonPath("$.eyeColor").value("#15803d"))
            .andExpect(jsonPath("$.outfitColor").value("#2563eb"));
    }

    @Test
    void updateMyAvatar_missingRequiredField_returns400() throws Exception {
        mockMvc.perform(put("/api/avatars/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "eyeColor": "#15803d",
                      "skinColor": "#D08B5B",
                      "hairColor": "#1a1a1a",
                      "hairStyle": "curly",
                      "outfit": "hoodie",
                      "outfitColor": "#dc2626"
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getMyOptions_returns200WithOptionsResponse() throws Exception {
        when(avatarService.getMyOptions()).thenReturn(new AvatarOptionsResponse(
            Map.of(
                "gender", List.of("neutral", "female", "male"),
                "hairStyle", List.of("short", "long")
            ),
            List.of(),
            false
        ));

        mockMvc.perform(get("/api/avatars/options"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.available.gender[0]").value("neutral"))
            .andExpect(jsonPath("$.available.hairStyle[0]").value("short"))
            .andExpect(jsonPath("$.medalLocked").isArray())
            .andExpect(jsonPath("$.colorPickerUnlocked").value(false));
    }
}
