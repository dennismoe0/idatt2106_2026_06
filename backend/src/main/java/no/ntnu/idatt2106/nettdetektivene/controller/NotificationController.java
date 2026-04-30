package no.ntnu.idatt2106.nettdetektivene.controller;

import lombok.RequiredArgsConstructor;
import no.ntnu.idatt2106.nettdetektivene.dto.notification.NotificationCountDto;
import no.ntnu.idatt2106.nettdetektivene.dto.notification.NotificationDto;
import no.ntnu.idatt2106.nettdetektivene.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class NotificationController {
    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationDto> listNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = currentUserId(userDetails);
        log.info("[NotificationController] GET /api/notifications userId={}", userId);
        return notificationService.listNotifications(userId);
    }

    @GetMapping("/count")
    public NotificationCountDto unreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = currentUserId(userDetails);
        log.info("[NotificationController] GET /api/notifications/count userId={}", userId);
        return notificationService.unreadCount(userId);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markRead(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long id
    ) {
        notificationService.markRead(currentUserId(userDetails), id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAllRead(currentUserId(userDetails));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long id
    ) {
        notificationService.deleteNotification(currentUserId(userDetails), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/old")
    public ResponseEntity<Void> deleteOldNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        notificationService.deleteOldNotifications(currentUserId(userDetails));
        return ResponseEntity.noContent().build();
    }

    private Long currentUserId(UserDetails userDetails) {
        return Long.parseLong(userDetails.getUsername());
    }
}
