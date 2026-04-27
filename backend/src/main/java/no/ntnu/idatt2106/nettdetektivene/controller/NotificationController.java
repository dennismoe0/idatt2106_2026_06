package no.ntnu.idatt2106.nettdetektivene.controller;

import lombok.RequiredArgsConstructor;
import no.ntnu.idatt2106.nettdetektivene.dto.notification.NotificationCountDto;
import no.ntnu.idatt2106.nettdetektivene.dto.notification.NotificationDto;
import no.ntnu.idatt2106.nettdetektivene.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationDto> listNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        return notificationService.listNotifications(currentUserId(userDetails));
    }

    @GetMapping("/count")
    public NotificationCountDto unreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        return notificationService.unreadCount(currentUserId(userDetails));
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

    private Long currentUserId(UserDetails userDetails) {
        return Long.parseLong(userDetails.getUsername());
    }
}
