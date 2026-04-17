package no.ntnu.idatt2106.nettdetektivene.controller;

import no.ntnu.idatt2106.nettdetektivene.dto.classroom.LeaderboardEntryDto;
import no.ntnu.idatt2106.nettdetektivene.dto.school.SchoolClassroomSummary;
import no.ntnu.idatt2106.nettdetektivene.dto.school.SchoolResponse;
import no.ntnu.idatt2106.nettdetektivene.security.JwtAuthFilter;
import no.ntnu.idatt2106.nettdetektivene.security.JwtTokenProvider;
import no.ntnu.idatt2106.nettdetektivene.security.UserDetailsServiceImpl;
import no.ntnu.idatt2106.nettdetektivene.service.SchoolService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SchoolController.class)
class SchoolControllerTest {

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
    SchoolService schoolService;

    @MockBean
    JwtAuthFilter jwtAuthFilter;

    @MockBean
    JwtTokenProvider jwtTokenProvider;

    @MockBean
    UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser(roles = "TEACHER", username = "1")
    void createSchool_returns201() throws Exception {
        when(schoolService.createSchool(eq(1L), any()))
            .thenReturn(new SchoolResponse(10L, "Solberg skole", "abc-123"));

        mockMvc.perform(post("/api/schools")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "name": "Solberg skole" }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(10))
            .andExpect(jsonPath("$.name").value("Solberg skole"))
            .andExpect(jsonPath("$.joinCode").value("abc-123"));
    }

    @Test
    @WithMockUser(roles = "TEACHER", username = "2")
    void joinSchool_returns200() throws Exception {
        when(schoolService.joinSchool(eq(2L), any()))
            .thenReturn(new SchoolResponse(5L, "Bakke ungdomsskole", "xyz-789"));

        mockMvc.perform(post("/api/schools/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "code": "xyz-789" }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Bakke ungdomsskole"))
            .andExpect(jsonPath("$.joinCode").value("xyz-789"));
    }

    @Test
    @WithMockUser(roles = "TEACHER", username = "3")
    void getMySchool_returns200() throws Exception {
        when(schoolService.getMySchool(3L))
            .thenReturn(new SchoolResponse(7L, "Fjord skole", "fjord-01"));

        mockMvc.perform(get("/api/schools/mine"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(7))
            .andExpect(jsonPath("$.name").value("Fjord skole"));
    }

    @Test
    @WithMockUser(roles = "TEACHER", username = "4")
    void getSchoolClassrooms_returns200() throws Exception {
        List<SchoolClassroomSummary> summaries = List.of(
            new SchoolClassroomSummary(1L, "Klasse 5A", "Digital detektivklasse", 20,
                List.of(new LeaderboardEntryDto("Agent Nora", 5, 7)))
        );
        when(schoolService.getSchoolClassrooms(4L)).thenReturn(summaries);

        mockMvc.perform(get("/api/schools/mine/classrooms"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].classroomId").value(1))
            .andExpect(jsonPath("$[0].name").value("Klasse 5A"));
    }
}
