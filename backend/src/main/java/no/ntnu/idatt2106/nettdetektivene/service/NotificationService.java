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

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    public static final String STUDENT_JOIN_REQUEST = "STUDENT_JOIN_REQUEST";
    public static final String MYSTERY_SUBMITTED = "MYSTERY_SUBMITTED";

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;

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

    @Transactional(readOnly = true)
    public List<NotificationDto> listNotifications(Long teacherId) {
        return notificationRepository.findByTeacher_IdOrderByCreatedAtDesc(teacherId).stream()
            .map(this::toDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public NotificationCountDto unreadCount(Long teacherId) {
        return new NotificationCountDto(notificationRepository.countByTeacher_IdAndIsReadFalse(teacherId));
    }

    @Transactional
    public void markRead(Long teacherId, Long notificationId) {
        Notification notification = getTeacherNotification(teacherId, notificationId);
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllRead(Long teacherId) {
        List<Notification> notifications = notificationRepository.findByTeacher_IdOrderByCreatedAtDesc(teacherId);
        notifications.stream()
            .filter(notification -> !notification.isRead())
            .forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(notifications);
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
