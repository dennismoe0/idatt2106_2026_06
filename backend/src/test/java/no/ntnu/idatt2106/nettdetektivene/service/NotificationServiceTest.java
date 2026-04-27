package no.ntnu.idatt2106.nettdetektivene.service;

import no.ntnu.idatt2106.nettdetektivene.entity.Classroom;
import no.ntnu.idatt2106.nettdetektivene.entity.Notification;
import no.ntnu.idatt2106.nettdetektivene.entity.User;
import no.ntnu.idatt2106.nettdetektivene.exception.ResourceNotFoundException;
import no.ntnu.idatt2106.nettdetektivene.repository.ClassroomRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.NotificationRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @Mock NotificationRepository notificationRepository;
    @Mock UserRepository userRepository;
    @Mock ClassroomRepository classroomRepository;
    @InjectMocks NotificationService notificationService;

    @Test
    void createNotification_savesReferenceAndClassroomContext() {
        User teacher = user(1L, User.Role.TEACHER);
        Classroom classroom = classroom(10L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(classroomRepository.findById(10L)).thenReturn(Optional.of(classroom));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(100L);
            notification.setCreatedAt(LocalDateTime.now());
            return notification;
        });

        var response = notificationService.createNotification(
            1L,
            10L,
            "STUDENT_JOIN_REQUEST",
            "Agent Nora wants to join 5A",
            2L
        );

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.classroomId()).isEqualTo(10L);
        assertThat(response.studentId()).isEqualTo(2L);
        assertThat(response.referenceId()).isEqualTo(2L);
        assertThat(response.isRead()).isFalse();

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getReferenceId()).isEqualTo(2L);
    }

    @Test
    void listNotifications_returnsTeacherNotifications() {
        Notification notification = notification(100L, 1L, 10L, "MYSTERY_SUBMITTED", 55L);
        when(notificationRepository.findByTeacher_IdOrderByCreatedAtDesc(1L)).thenReturn(List.of(notification));

        var result = notificationService.listNotifications(1L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(100L);
        assertThat(result.getFirst().referenceId()).isEqualTo(55L);
        assertThat(result.getFirst().studentId()).isNull();
    }

    @Test
    void markRead_onlyAllowsOwningTeacher() {
        Notification notification = notification(100L, 1L, 10L, "STUDENT_JOIN_REQUEST", 2L);
        when(notificationRepository.findByIdAndTeacher_Id(100L, 1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        var response = notificationService.markRead(1L, 100L);

        assertThat(response.isRead()).isTrue();
        verify(notificationRepository).save(notification);
    }

    @Test
    void markRead_missingNotificationThrows() {
        when(notificationRepository.findByIdAndTeacher_Id(100L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markRead(1L, 100L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Notification not found");
    }

    private Notification notification(Long id, Long teacherId, Long classroomId, String type, Long referenceId) {
        Notification notification = new Notification();
        notification.setId(id);
        notification.setTeacher(user(teacherId, User.Role.TEACHER));
        notification.setClassroom(classroom(classroomId));
        notification.setType(type);
        notification.setMessage("message");
        notification.setReferenceId(referenceId);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }

    private User user(Long id, User.Role role) {
        User user = new User();
        user.setId(id);
        user.setEmail("user" + id + "@test.no");
        user.setPasswordHash("hash");
        user.setRole(role);
        return user;
    }

    private Classroom classroom(Long id) {
        Classroom classroom = new Classroom();
        classroom.setId(id);
        classroom.setName("5A");
        classroom.setJoinCode("fjord-tiger");
        return classroom;
    }
}
