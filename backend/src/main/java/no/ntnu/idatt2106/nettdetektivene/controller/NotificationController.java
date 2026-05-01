package no.ntnu.idatt2106.nettdetektivene.controller;

import lombok.RequiredArgsConstructor;
import no.ntnu.idatt2106.nettdetektivene.dto.notification.NotificationCountDto;
import no.ntnu.idatt2106.nettdetektivene.dto.notification.NotificationDto;
import no.ntnu.idatt2106.nettdetektivene.service.NotificationService;
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

/**
 * Handles notification management for teachers, including listing, marking as read, and deleting notifications.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class NotificationController {
    private final NotificationService notificationService;

    /**
     * Returns all notifications for the authenticated teacher.
     *
     * @param userDetails the authenticated teacher
     * @return a list of {@link NotificationDto} for the teacher
     */
    @GetMapping
    public List<NotificationDto> listNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        return notificationService.listNotifications(currentUserId(userDetails));
    }

    /**
     * Returns the count of unread notifications for the authenticated teacher.
     *
     * @param userDetails the authenticated teacher
     * @return a {@link NotificationCountDto} containing the unread count
     */
    @GetMapping("/count")
    public NotificationCountDto unreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        return notificationService.unreadCount(currentUserId(userDetails));
    }

    /**
     * Marks a specific notification as read for the authenticated teacher.
     *
     * @param userDetails the authenticated teacher
     * @param id          the ID of the notification to mark as read
     * @return 204 No Content on success
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markRead(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long id
    ) {
        notificationService.markRead(currentUserId(userDetails), id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Marks all notifications as read for the authenticated teacher.
     *
     * @param userDetails the authenticated teacher
     * @return 204 No Content on success
     */
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAllRead(currentUserId(userDetails));
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes a specific notification belonging to the authenticated teacher.
     *
     * @param userDetails the authenticated teacher
     * @param id          the ID of the notification to delete
     * @return 204 No Content on success
     */
    @PreAuthorize("hasRole('TEACHER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long id
    ) {
        notificationService.deleteNotification(currentUserId(userDetails), id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes all old (read/expired) notifications for the authenticated teacher.
     *
     * @param userDetails the authenticated teacher
     * @return 204 No Content on success
     */
    @PreAuthorize("hasRole('TEACHER')")
    @DeleteMapping("/old")
    public ResponseEntity<Void> deleteOldNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        notificationService.deleteOldNotifications(currentUserId(userDetails));
        return ResponseEntity.noContent().build();
    }

    /**
     * Extracts the numeric user ID from the authenticated principal's username.
     *
     * @param userDetails the authenticated user
     * @return the user's database ID
     */
    private Long currentUserId(UserDetails userDetails) {
        return Long.parseLong(userDetails.getUsername());
    }
}
