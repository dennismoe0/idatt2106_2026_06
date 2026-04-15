package no.ntnu.idatt2106.nettdetektivene.controller;

import no.ntnu.idatt2106.nettdetektivene.config.SecurityConfig;
import no.ntnu.idatt2106.nettdetektivene.security.JwtAuthFilter;
import no.ntnu.idatt2106.nettdetektivene.security.JwtTokenProvider;
import no.ntnu.idatt2106.nettdetektivene.security.UserDetailsServiceImpl;
import no.ntnu.idatt2106.nettdetektivene.service.AvatarService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AvatarController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
class AvatarControllerSecurityTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    AvatarService avatarService;

    @MockBean
    JwtTokenProvider jwtTokenProvider;

    @MockBean
    UserDetailsServiceImpl userDetailsService;

    @Test
    void getMyAvatar_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/avatars/me"))
            .andExpect(status().isForbidden());
    }
}
