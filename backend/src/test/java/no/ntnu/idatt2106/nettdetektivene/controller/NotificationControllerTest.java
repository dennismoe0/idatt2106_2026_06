package no.ntnu.idatt2106.nettdetektivene.controller;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import no.ntnu.idatt2106.nettdetektivene.config.SecurityConfig;
import no.ntnu.idatt2106.nettdetektivene.dto.notification.NotificationCountDto;
import no.ntnu.idatt2106.nettdetektivene.dto.notification.NotificationDto;
import no.ntnu.idatt2106.nettdetektivene.security.JwtAuthFilter;
import no.ntnu.idatt2106.nettdetektivene.security.JwtTokenProvider;
import no.ntnu.idatt2106.nettdetektivene.security.UserDetailsServiceImpl;
import no.ntnu.idatt2106.nettdetektivene.service.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
class NotificationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    NotificationService notificationService;

    @MockBean
    JwtTokenProvider jwtTokenProvider;

    @MockBean
    UserDetailsServiceImpl userDetailsService;

    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void setUpLogCapture() {
        Logger logger = (Logger) LoggerFactory.getLogger(NotificationController.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);
    }

    @AfterEach
    void tearDownLogCapture() {
        Logger logger = (Logger) LoggerFactory.getLogger(NotificationController.class);
        logger.detachAppender(logAppender);
    }

    @Test
    @WithMockUser(roles = "TEACHER", username = "42")
    void listNotifications_returns200_andLogsRequest() throws Exception {
        when(notificationService.listNotifications(42L)).thenReturn(List.of(
            new NotificationDto(1L, "MYSTERY_SUBMITTED", "Ny melding", 10L, 7L, 8L, false, LocalDateTime.now())
        ));

        mockMvc.perform(get("/api/notifications"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].type").value("MYSTERY_SUBMITTED"))
            .andExpect(jsonPath("$[0].message").value("Ny melding"))
            .andExpect(jsonPath("$[0].isRead").value(false));

        assertThat(logAppender.list)
            .anySatisfy(event -> assertThat(event.getFormattedMessage())
                .isEqualTo("[NotificationController] GET /api/notifications userId=42"));
    }

    @Test
    @WithMockUser(roles = "TEACHER", username = "99")
    void unreadCount_returns200_andLogsRequest() throws Exception {
        when(notificationService.unreadCount(99L)).thenReturn(new NotificationCountDto(3));

        mockMvc.perform(get("/api/notifications/count"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(3));

        assertThat(logAppender.list)
            .anySatisfy(event -> assertThat(event.getFormattedMessage())
                .isEqualTo("[NotificationController] GET /api/notifications/count userId=99"));
    }

    @Test
    @WithMockUser(roles = "STUDENT", username = "5")
    void deleteNotification_studentIsForbidden_viaClassLevelPreAuthorize() throws Exception {
        mockMvc.perform(delete("/api/notifications/12"))
            .andExpect(status().isForbidden());

        verify(notificationService, never()).deleteNotification(5L, 12L);
    }

    @Test
    @WithMockUser(roles = "STUDENT", username = "5")
    void deleteOldNotifications_studentIsForbidden_viaClassLevelPreAuthorize() throws Exception {
        mockMvc.perform(delete("/api/notifications/old"))
            .andExpect(status().isForbidden());

        verify(notificationService, never()).deleteOldNotifications(5L);
    }
}
