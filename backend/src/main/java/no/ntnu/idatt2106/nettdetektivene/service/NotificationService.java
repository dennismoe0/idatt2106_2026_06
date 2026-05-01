package no.ntnu.idatt2106.nettdetektivene.service;

import lombok.RequiredArgsConstructor;
import no.ntnu.idatt2106.nettdetektivene.dto.notification.NotificationCountDto;
import no.ntnu.idatt2106.nettdetektivene.dto.notification.NotificationDto;
import no.ntnu.idatt2106.nettdetektivene.entity.Classroom;
import no.ntnu.idatt2106.nettdetektivene.entity.Notification;
import no.ntnu.idatt2106.nettdetektivene.entity.User;
import no.ntnu.idatt2106.nettdetektivene.exception.ResourceNotFoundException;
import no.ntnu.idatt2106.nettdetektivene.repository.ClassroomRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.NotificationRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Creates and manages teacher notifications, such as student join requests and weekly mystery submissions.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {
    public static final String STUDENT_JOIN_REQUEST = "STUDENT_JOIN_REQUEST";
    public static final String MYSTERY_SUBMITTED = "MYSTERY_SUBMITTED";

    private static final long STALE_HOURS = 8;

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;

    /**
     * Creates and persists a notification for a teacher.
     *
     * @param teacherId   the recipient teacher's user ID
     * @param classroomId the classroom related to the notification
     * @param type        the notification type constant (e.g. {@code STUDENT_JOIN_REQUEST})
     * @param message     the human-readable message text
     * @param referenceId an optional related entity ID (e.g. student ID or mystery ID)
     * @return the created {@link NotificationDto}
     */
    @Transactional
    public NotificationDto createNotification(
        Long teacherId,
        Long classroomId,
        String type,
        String message,
        Long referenceId
    ) {
        User teacher = userRepository.findById(teacherId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Classroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(() -> new ResourceNotFoundException("Classroom not found"));

        Notification notification = new Notification();
        notification.setTeacher(teacher);
        notification.setClassroom(classroom);
        notification.setType(type);
        notification.setMessage(message);
        notification.setReferenceId(referenceId);

        return toDto(notificationRepository.save(notification));
    }

    /**
     * Returns all notifications for a teacher, newest first.
     *
     * @param teacherId the teacher's user ID
     * @return list of {@link NotificationDto}
     */
    @Transactional(readOnly = true)
    public List<NotificationDto> listNotifications(Long teacherId) {
        return notificationRepository.findByTeacher_IdOrderByCreatedAtDesc(teacherId).stream()
            .map(this::toDto)
            .toList();
    }

    /**
     * Returns the count of unread notifications for a teacher.
     *
     * @param teacherId the teacher's user ID
     * @return a {@link NotificationCountDto} with the unread count
     */
    @Transactional(readOnly = true)
    public NotificationCountDto unreadCount(Long teacherId) {
        return new NotificationCountDto(notificationRepository.countByTeacher_IdAndIsReadFalse(teacherId));
    }

    /**
     * Marks a single notification as read.
     *
     * @param teacherId      the teacher's user ID
     * @param notificationId the notification ID
     */
    @Transactional
    public void markRead(Long teacherId, Long notificationId) {
        Notification notification = getTeacherNotification(teacherId, notificationId);
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    /**
     * Marks all notifications for the given teacher as read.
     *
     * @param teacherId the teacher's user ID
     */
    @Transactional
    public void markAllRead(Long teacherId) {
        List<Notification> notifications = notificationRepository.findByTeacher_IdOrderByCreatedAtDesc(teacherId);
        notifications.stream()
            .filter(notification -> !notification.isRead())
            .forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(notifications);
    }

    /**
     * Deletes a single notification belonging to the teacher.
     *
     * @param teacherId      the teacher's user ID
     * @param notificationId the notification ID to delete
     */
    @Transactional
    public void deleteNotification(Long teacherId, Long notificationId) {
        Notification notification = getTeacherNotification(teacherId, notificationId);
        notificationRepository.delete(notification);
    }

    /**
     * Deletes notifications older than {@code STALE_HOURS} for the given teacher.
     *
     * @param teacherId the teacher's user ID
     * @return the number of notifications deleted
     */
    @Transactional
    public int deleteOldNotifications(Long teacherId) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(STALE_HOURS);
        return notificationRepository.deleteByTeacher_IdAndCreatedAtBefore(teacherId, cutoff);
    }

    private Notification getTeacherNotification(Long teacherId, Long notificationId) {
        return notificationRepository.findByIdAndTeacher_Id(notificationId, teacherId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
    }

    private NotificationDto toDto(Notification notification) {
        Long classroomId = notification.getClassroom() != null ? notification.getClassroom().getId() : null;
        Long studentId = STUDENT_JOIN_REQUEST.equals(notification.getType()) ? notification.getReferenceId() : null;
        return new NotificationDto(
            notification.getId(),
            notification.getType(),
            notification.getMessage(),
            notification.getReferenceId(),
            classroomId,
            studentId,
            notification.isRead(),
            notification.getCreatedAt()
        );
    }
}