package no.ntnu.idatt2106.nettdetektivene.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.ntnu.idatt2106.nettdetektivene.dto.game.SubmitAnswerRequest;
import no.ntnu.idatt2106.nettdetektivene.entity.Classroom;
import no.ntnu.idatt2106.nettdetektivene.entity.Medal;
import no.ntnu.idatt2106.nettdetektivene.entity.StudentMedal;
import no.ntnu.idatt2106.nettdetektivene.entity.StudentProgress;
import no.ntnu.idatt2106.nettdetektivene.entity.Stop;
import no.ntnu.idatt2106.nettdetektivene.entity.Task;
import no.ntnu.idatt2106.nettdetektivene.entity.TaskType;
import no.ntnu.idatt2106.nettdetektivene.entity.User;
import no.ntnu.idatt2106.nettdetektivene.repository.ClassroomRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.MedalRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.StopRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.StudentMedalRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.StudentProgressRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.TaskRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    private static final Long STUDENT_ID = 7L;
    private static final Long CLASSROOM_ID = 11L;

    @Mock StopRepository stopRepository;
    @Mock TaskRepository taskRepository;
    @Mock StudentProgressRepository studentProgressRepository;
    @Mock MedalRepository medalRepository;
    @Mock StudentMedalRepository studentMedalRepository;
    @Mock UserRepository userRepository;
    @Mock ClassroomRepository classroomRepository;

    GameService gameService;

    @BeforeEach
    void setUp() {
        gameService = new GameService(
            stopRepository,
            taskRepository,
            studentProgressRepository,
            medalRepository,
            studentMedalRepository,
            userRepository,
            classroomRepository,
            new ObjectMapper()
        );
    }

    @Test
    void getStops_firstStopAlwaysUnlocked() {
        Stop first = stop(1L, 1, "Nyhetskvartalet");
        when(stopRepository.findAllByOrderByOrderIndexAsc()).thenReturn(List.of(first));
        when(taskRepository.countByStop_Id(1L)).thenReturn(3L);
        when(studentProgressRepository.countByStudent_IdAndTask_Stop_IdAndClassroom_IdAndCompletedTrue(
            STUDENT_ID, 1L, CLASSROOM_ID
        )).thenReturn(0L);

        var response = gameService.getStops(STUDENT_ID, CLASSROOM_ID);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().locked()).isFalse();
        assertThat(response.getFirst().taskCount()).isEqualTo(3);
    }

    @Test
    void getStops_secondStopLockedUntilFirstComplete() {
        Stop first = stop(1L, 1, "Nyhetskvartalet");
        Stop second = stop(2L, 2, "Postkontoret");
        when(stopRepository.findAllByOrderByOrderIndexAsc()).thenReturn(List.of(first, second));
        when(taskRepository.countByStop_Id(1L)).thenReturn(2L);
        when(taskRepository.countByStop_Id(2L)).thenReturn(3L);
        when(studentProgressRepository.countByStudent_IdAndTask_Stop_IdAndClassroom_IdAndCompletedTrue(
            STUDENT_ID, 1L, CLASSROOM_ID
        )).thenReturn(1L);
        when(studentProgressRepository.countByStudent_IdAndTask_Stop_IdAndClassroom_IdAndCompletedTrue(
            STUDENT_ID, 2L, CLASSROOM_ID
        )).thenReturn(0L);

        var response = gameService.getStops(STUDENT_ID, CLASSROOM_ID);

        assertThat(response.get(0).locked()).isFalse();
        assertThat(response.get(1).locked()).isTrue();
    }

    @Test
    void getStops_zeroTaskPreviousStopDoesNotUnlockNextStop() {
        Stop first = stop(1L, 1, "Nyhetskvartalet");
        Stop second = stop(2L, 2, "Postkontoret");
        when(stopRepository.findAllByOrderByOrderIndexAsc()).thenReturn(List.of(first, second));
        when(taskRepository.countByStop_Id(1L)).thenReturn(0L);
        when(taskRepository.countByStop_Id(2L)).thenReturn(1L);
        when(studentProgressRepository.countByStudent_IdAndTask_Stop_IdAndClassroom_IdAndCompletedTrue(
            STUDENT_ID, 2L, CLASSROOM_ID
        )).thenReturn(0L);

        var response = gameService.getStops(STUDENT_ID, CLASSROOM_ID);

        assertThat(response.get(1).locked()).isTrue();
    }

    @Test
    void submitAnswer_correct_recordsProgress() {
        Stop stop = stop(1L, 1, "Nyhetskvartalet");
        Task task = fakeNewsTask(20L, stop);
        when(taskRepository.findById(20L)).thenReturn(Optional.of(task));
        when(studentProgressRepository.findByStudent_IdAndTask_IdAndClassroom_Id(
            STUDENT_ID, 20L, CLASSROOM_ID
        )).thenReturn(Optional.empty());
        when(userRepository.getReferenceById(STUDENT_ID)).thenReturn(user());
        when(classroomRepository.getReferenceById(CLASSROOM_ID)).thenReturn(classroom());
        when(taskRepository.countByStop_Id(1L)).thenReturn(3L);
        when(studentProgressRepository.countByStudent_IdAndTask_Stop_IdAndClassroom_IdAndCompletedTrue(
            STUDENT_ID, 1L, CLASSROOM_ID
        )).thenReturn(1L);

        var response = gameService.submitAnswer(
            STUDENT_ID,
            CLASSROOM_ID,
            20L,
            new SubmitAnswerRequest(Map.of("article_0", true, "article_1", false))
        );

        assertThat(response.correct()).isTrue();
        assertThat(response.score()).isEqualTo(100);

        ArgumentCaptor<StudentProgress> captor = ArgumentCaptor.forClass(StudentProgress.class);
        verify(studentProgressRepository).save(captor.capture());
        assertThat(captor.getValue().isCompleted()).isTrue();
        assertThat(captor.getValue().getScore()).isEqualTo(100);
        assertThat(captor.getValue().getAttempts()).isEqualTo(1);
    }

    @Test
    void submitAnswer_stopComplete_awardsMedal() {
        Stop stop = stop(1L, 1, "Nyhetskvartalet");
        Task task = fakeNewsTask(20L, stop);
        Medal medal = medal(30L, stop);

        when(taskRepository.findById(20L)).thenReturn(Optional.of(task));
        when(studentProgressRepository.findByStudent_IdAndTask_IdAndClassroom_Id(
            STUDENT_ID, 20L, CLASSROOM_ID
        )).thenReturn(Optional.empty());
        when(userRepository.getReferenceById(STUDENT_ID)).thenReturn(user());
        when(classroomRepository.getReferenceById(CLASSROOM_ID)).thenReturn(classroom());
        when(taskRepository.countByStop_Id(1L)).thenReturn(1L);
        when(studentProgressRepository.countByStudent_IdAndTask_Stop_IdAndClassroom_IdAndCompletedTrue(
            STUDENT_ID, 1L, CLASSROOM_ID
        )).thenReturn(1L);
        when(medalRepository.findByStop_Id(1L)).thenReturn(Optional.of(medal));
        when(studentMedalRepository.existsByStudent_IdAndMedal_IdAndClassroom_Id(
            STUDENT_ID, 30L, CLASSROOM_ID
        )).thenReturn(false);

        var response = gameService.submitAnswer(
            STUDENT_ID,
            CLASSROOM_ID,
            20L,
            new SubmitAnswerRequest(Map.of("article_0", true, "article_1", false))
        );

        assertThat(response.stopCompleted()).isTrue();
        assertThat(response.medalEarned()).isNotNull();
        assertThat(response.medalEarned().id()).isEqualTo(30L);
        verify(studentMedalRepository).save(any(StudentMedal.class));
    }

    @Test
    void submitAnswer_alreadyCompleted_doesNotDuplicate() {
        Stop stop = stop(1L, 1, "Nyhetskvartalet");
        Task task = fakeNewsTask(20L, stop);
        StudentProgress progress = new StudentProgress();
        progress.setCompleted(true);
        progress.setScore(100);

        when(taskRepository.findById(20L)).thenReturn(Optional.of(task));
        when(studentProgressRepository.findByStudent_IdAndTask_IdAndClassroom_Id(
            STUDENT_ID, 20L, CLASSROOM_ID
        )).thenReturn(Optional.of(progress));
        when(taskRepository.countByStop_Id(1L)).thenReturn(1L);
        when(studentProgressRepository.countByStudent_IdAndTask_Stop_IdAndClassroom_IdAndCompletedTrue(
            STUDENT_ID, 1L, CLASSROOM_ID
        )).thenReturn(1L);

        var response = gameService.submitAnswer(
            STUDENT_ID,
            CLASSROOM_ID,
            20L,
            new SubmitAnswerRequest(Map.of("article_0", true, "article_1", false))
        );

        assertThat(response.correct()).isTrue();
        assertThat(response.medalEarned()).isNull();
        verify(studentProgressRepository, never()).save(any(StudentProgress.class));
        verify(studentMedalRepository, never()).save(any(StudentMedal.class));
    }

    @Test
    void getTasks_lockedStop_throws() {
        Stop first = stop(1L, 1, "Nyhetskvartalet");
        Stop second = stop(2L, 2, "Postkontoret");
        when(stopRepository.findById(2L)).thenReturn(Optional.of(second));
        when(stopRepository.findAllByOrderByOrderIndexAsc()).thenReturn(List.of(first, second));
        when(taskRepository.countByStop_Id(1L)).thenReturn(1L);
        when(studentProgressRepository.countByStudent_IdAndTask_Stop_IdAndClassroom_IdAndCompletedTrue(
            STUDENT_ID, 1L, CLASSROOM_ID
        )).thenReturn(0L);

        assertThatThrownBy(() -> gameService.getTasks(STUDENT_ID, CLASSROOM_ID, 2L))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Stop is locked");
    }

    @Test
    void getTask_fakeNews_sanitizesAnswerFieldsFromContent() throws Exception {
        Stop stop = stop(1L, 1, "Nyhetskvartalet");
        Task task = fakeNewsTask(20L, stop);
        when(taskRepository.findById(20L)).thenReturn(Optional.of(task));
        when(studentProgressRepository.findByStudent_IdAndTask_IdAndClassroom_Id(
            STUDENT_ID, 20L, CLASSROOM_ID
        )).thenReturn(Optional.empty());

        var response = gameService.getTask(STUDENT_ID, CLASSROOM_ID, 20L);
        var content = new ObjectMapper().readTree(response.contentJson());

        assertThat(content.has("explanation")).isFalse();
        assertThat(content.path("articles").get(0).has("isReal")).isFalse();
        assertThat(content.path("articles").get(1).has("isReal")).isFalse();
    }

    @Test
    void getTask_phishingEmail_sanitizesAnswerFieldsFromContent() throws Exception {
        Stop stop = stop(2L, 1, "Postkontoret");
        Task task = phishingTask(21L, stop);
        when(taskRepository.findById(21L)).thenReturn(Optional.of(task));
        when(studentProgressRepository.findByStudent_IdAndTask_IdAndClassroom_Id(
            STUDENT_ID, 21L, CLASSROOM_ID
        )).thenReturn(Optional.empty());

        var response = gameService.getTask(STUDENT_ID, CLASSROOM_ID, 21L);
        var content = new ObjectMapper().readTree(response.contentJson());

        assertThat(content.has("explanation")).isFalse();
        assertThat(content.path("email").has("correctAction")).isFalse();
        assertThat(content.path("email").has("suspiciousElements")).isFalse();
    }

    @Test
    void submitAnswer_wrongAnswer_doesNotRecordCompletedProgress() {
        Stop stop = stop(1L, 1, "Nyhetskvartalet");
        Task task = fakeNewsTask(20L, stop);
        when(taskRepository.findById(20L)).thenReturn(Optional.of(task));
        when(studentProgressRepository.findByStudent_IdAndTask_IdAndClassroom_Id(
            STUDENT_ID, 20L, CLASSROOM_ID
        )).thenReturn(Optional.empty());

        var response = gameService.submitAnswer(
            STUDENT_ID,
            CLASSROOM_ID,
            20L,
            new SubmitAnswerRequest(Map.of("article_0", true, "article_1", true))
        );

        assertThat(response.correct()).isFalse();
        assertThat(response.score()).isZero();
        verify(studentProgressRepository, never()).save(any(StudentProgress.class));
    }

    @Test
    void submitAnswer_fakeNews_checksAllArticles() {
        Stop stop = stop(1L, 1, "Nyhetskvartalet");
        Task task = fakeNewsTask(20L, stop);
        when(taskRepository.findById(20L)).thenReturn(Optional.of(task));
        when(studentProgressRepository.findByStudent_IdAndTask_IdAndClassroom_Id(
            STUDENT_ID, 20L, CLASSROOM_ID
        )).thenReturn(Optional.empty());

        var response = gameService.submitAnswer(
            STUDENT_ID,
            CLASSROOM_ID,
            20L,
            new SubmitAnswerRequest(Map.of("article_0", true))
        );

        assertThat(response.correct()).isFalse();
        verify(studentProgressRepository, never()).save(any(StudentProgress.class));
    }

    @Test
    void submitAnswer_phishingEmail_checksAction() {
        Stop stop = stop(2L, 1, "Postkontoret");
        Task task = phishingTask(21L, stop);
        when(taskRepository.findById(21L)).thenReturn(Optional.of(task));
        when(studentProgressRepository.findByStudent_IdAndTask_IdAndClassroom_Id(
            STUDENT_ID, 21L, CLASSROOM_ID
        )).thenReturn(Optional.empty());
        when(userRepository.getReferenceById(STUDENT_ID)).thenReturn(user());
        when(classroomRepository.getReferenceById(CLASSROOM_ID)).thenReturn(classroom());
        when(taskRepository.countByStop_Id(2L)).thenReturn(2L);
        when(studentProgressRepository.countByStudent_IdAndTask_Stop_IdAndClassroom_IdAndCompletedTrue(
            STUDENT_ID, 2L, CLASSROOM_ID
        )).thenReturn(1L);

        var response = gameService.submitAnswer(
            STUDENT_ID,
            CLASSROOM_ID,
            21L,
            new SubmitAnswerRequest(Map.of("action", "REPORT"))
        );

        assertThat(response.correct()).isTrue();
        verify(studentProgressRepository).save(any(StudentProgress.class));
    }

    private Stop stop(Long id, int orderIndex, String name) {
        Stop stop = new Stop();
        stop.setId(id);
        stop.setOrderIndex(orderIndex);
        stop.setName(name);
        stop.setDescription(name + " description");
        stop.setTheme("theme");
        return stop;
    }

    private Task fakeNewsTask(Long id, Stop stop) {
        Task task = task(id, stop, TaskType.FAKE_NEWS);
        task.setContentJson("""
            {
              "articles": [
                { "headline": "Ekte", "body": "Sant", "source": "Kilde", "isReal": true },
                { "headline": "Falsk", "body": "Usant", "source": "Ukjent", "isReal": false }
              ],
              "explanation": "Den andre artikkelen er falsk."
            }
            """);
        task.setCorrectAnswerJson("""
            { "article_0": true, "article_1": false }
            """);
        return task;
    }

    private Task phishingTask(Long id, Stop stop) {
        Task task = task(id, stop, TaskType.PHISHING_EMAIL);
        task.setContentJson("""
            {
              "email": {
                "fromName": "DNB Kundeservice",
                "fromEmail": "support@dnb-kundeservice.com",
                "subject": "Viktig",
                "body": "Klikk her",
                "suspiciousElements": ["fromEmail"],
                "correctAction": "REPORT"
              },
              "explanation": "Avsenderen er mistenkelig."
            }
            """);
        task.setCorrectAnswerJson("""
            { "action": "REPORT" }
            """);
        return task;
    }

    private Task task(Long id, Stop stop, TaskType taskType) {
        Task task = new Task();
        task.setId(id);
        task.setStop(stop);
        task.setTaskType(taskType);
        task.setContentJson("{}");
        task.setCorrectAnswerJson("{}");
        task.setGuidanceText("Guidance");
        return task;
    }

    private Medal medal(Long id, Stop stop) {
        Medal medal = new Medal();
        medal.setId(id);
        medal.setStop(stop);
        medal.setName("Medal");
        medal.setDescription("Description");
        return medal;
    }

    private User user() {
        User user = new User();
        user.setId(STUDENT_ID);
        user.setRole(User.Role.STUDENT);
        user.setEmail("student@test.no");
        user.setPasswordHash("hash");
        return user;
    }

    private Classroom classroom() {
        Classroom classroom = new Classroom();
        classroom.setId(CLASSROOM_ID);
        classroom.setName("5A");
        classroom.setJoinCode("fjord-tiger");
        return classroom;
    }
}
