package no.ntnu.idatt2106.nettdetektivene.service;

import no.ntnu.idatt2106.nettdetektivene.dto.MysteryCompleteDto;
import no.ntnu.idatt2106.nettdetektivene.dto.MysteryCompleteResultDto;
import no.ntnu.idatt2106.nettdetektivene.dto.WeeklyMysterySubmissionDto;
import no.ntnu.idatt2106.nettdetektivene.entity.*;
import no.ntnu.idatt2106.nettdetektivene.model.ClassroomStudentStatus;
import no.ntnu.idatt2106.nettdetektivene.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeeklyMysteryServiceTest {

    @Mock WeeklyMysteryRepository mysteryRepo;
    @Mock StudentMysteryCompletionRepository completionRepo;
    @Mock MedalRepository medalRepo;
    @Mock StudentMedalRepository studentMedalRepo;
    @Mock UserRepository userRepo;
    @Mock ClassroomRepository classroomRepo;
    @Mock ClassroomStudentRepository classroomStudentRepo;
    @Mock ClassroomTeacherRepository classroomTeacherRepo;
    @Mock NotificationService notificationService;

    @InjectMocks WeeklyMysteryService service;

    @Test
    void completeMystery_correctAnswer_returnsCorrectResultWithRewards() {
        User student = new User();
        student.setId(1L);

        Classroom classroom = new Classroom();
        classroom.setId(10L);

        WeeklyMystery mystery = new WeeklyMystery();
        mystery.setId(5L);
        mystery.setCorrectAnswer("FAKE");
        mystery.setRewardStars(5);
        mystery.setRewardXp(50);
        mystery.setFeatured(true);
        mystery.setStatus(WeeklyMystery.Status.APPROVED);
        mystery.setClassroom(classroom);

        when(classroomStudentRepo.existsByClassroom_IdAndStudent_IdAndStatus(10L, 1L, ClassroomStudentStatus.APPROVED))
            .thenReturn(true);
        when(mysteryRepo.findByClassroomIdAndFeaturedTrue(10L)).thenReturn(Optional.of(mystery));
        when(completionRepo.existsByStudentIdAndMysteryId(1L, 5L)).thenReturn(false);
        when(completionRepo.countByStudentIdAndCorrectTrue(1L)).thenReturn(0L);
        when(completionRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(userRepo.findById(1L)).thenReturn(Optional.of(student));

        MysteryCompleteDto dto = new MysteryCompleteDto(10L, "FAKE");
        MysteryCompleteResultDto result = service.completeMystery(student, dto);

        assertThat(result.correct()).isTrue();
        assertThat(result.starsEarned()).isEqualTo(5);
        assertThat(result.xpEarned()).isEqualTo(50);
    }

    @Test
    void completeMystery_alreadyCompleted_throwsException() {
        User student = new User();
        student.setId(1L);

        Classroom classroom = new Classroom();
        classroom.setId(10L);

        WeeklyMystery mystery = new WeeklyMystery();
        mystery.setId(5L);
        mystery.setClassroom(classroom);
        mystery.setFeatured(true);

        when(classroomStudentRepo.existsByClassroom_IdAndStudent_IdAndStatus(10L, 1L, ClassroomStudentStatus.APPROVED))
            .thenReturn(true);
        when(mysteryRepo.findByClassroomIdAndFeaturedTrue(10L)).thenReturn(Optional.of(mystery));
        when(completionRepo.existsByStudentIdAndMysteryId(1L, 5L)).thenReturn(true);

        MysteryCompleteDto dto = new MysteryCompleteDto(10L, "FAKE");

        org.junit.jupiter.api.Assertions.assertThrows(
            IllegalStateException.class,
            () -> service.completeMystery(student, dto)
        );
    }

    @Test
    void submitMystery_notifiesTeachersWithMysteryReference() {
        User student = new User();
        student.setId(1L);

        User teacher = new User();
        teacher.setId(2L);

        Classroom classroom = new Classroom();
        classroom.setId(10L);

        when(classroomRepo.findById(10L)).thenReturn(Optional.of(classroom));
        when(mysteryRepo.save(any(WeeklyMystery.class))).thenAnswer(invocation -> {
            WeeklyMystery mystery = invocation.getArgument(0);
            mystery.setId(55L);
            return mystery;
        });
        when(classroomTeacherRepo.findTeachersByClassroomId(10L)).thenReturn(java.util.List.of(teacher));

        WeeklyMystery result = service.submitMystery(
            student,
            new WeeklyMysterySubmissionDto("Mystery title", "Description", null, 10L)
        );

        assertThat(result.getId()).isEqualTo(55L);
        verify(notificationService).createNotification(
            2L,
            10L,
            NotificationService.MYSTERY_SUBMITTED,
            "Nytt ukens mysterium sendt inn: Mystery title",
            55L
        );
    }
}
