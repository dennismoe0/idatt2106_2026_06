package no.ntnu.idatt2106.nettdetektivene.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.ntnu.idatt2106.nettdetektivene.dto.game.ClaimXpResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.game.PlayerProfileDto;
import no.ntnu.idatt2106.nettdetektivene.dto.game.SubmitAnswerRequest;
import no.ntnu.idatt2106.nettdetektivene.entity.Medal;
import no.ntnu.idatt2106.nettdetektivene.entity.StudentMedal;
import no.ntnu.idatt2106.nettdetektivene.entity.StudentProgress;
import no.ntnu.idatt2106.nettdetektivene.entity.StudentXpLog;
import no.ntnu.idatt2106.nettdetektivene.entity.Stop;
import no.ntnu.idatt2106.nettdetektivene.entity.Task;
import no.ntnu.idatt2106.nettdetektivene.entity.TaskType;
import no.ntnu.idatt2106.nettdetektivene.entity.User;
import no.ntnu.idatt2106.nettdetektivene.repository.ClassroomRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.MedalRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.StopRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.StudentMedalRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.StudentProgressRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.StudentXpLogRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.TaskRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.UserRepository;
import no.ntnu.idatt2106.nettdetektivene.service.answer.AiPhotoTaskAnswerChecker;
import no.ntnu.idatt2106.nettdetektivene.service.answer.FakeNewsTaskAnswerChecker;
import no.ntnu.idatt2106.nettdetektivene.service.answer.MarketplaceTaskAnswerChecker;
import no.ntnu.idatt2106.nettdetektivene.service.answer.PasswordStrengthEvaluator;
import no.ntnu.idatt2106.nettdetektivene.service.answer.PasswordTaskAnswerChecker;
import no.ntnu.idatt2106.nettdetektivene.service.answer.PhishingEmailTaskAnswerChecker;
import no.ntnu.idatt2106.nettdetektivene.service.answer.SocialMediaTaskAnswerChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
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
    @Mock NotebookService notebookService;
    @Mock StudentXpLogRepository studentXpLogRepository;

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
            new ObjectMapper(),
            notebookService,
            studentXpLogRepository,
            List.of(
                new FakeNewsTaskAnswerChecker(),
                new PhishingEmailTaskAnswerChecker(),
                new AiPhotoTaskAnswerChecker(),
                new MarketplaceTaskAnswerChecker(),
                new PasswordTaskAnswerChecker(new ObjectMapper(), new PasswordStrengthEvaluator()),
                new SocialMediaTaskAnswerChecker()
            )
        );
    }

    // ─── getStops ────────────────────────────────────────────────────────────

    @Test
    void getStops_firstStopAlwaysUnlocked() {
        Stop first = stop(1L, 1, "Nyhetskvartalet");
        when(classroomRepository.existsById(CLASSROOM_ID)).thenReturn(true);
        when(stopRepository.findAllByOrderByOrderIndexAsc()).thenReturn(List.of(first));
        when(taskRepository.countByStop_Id(1L)).thenReturn(3L);
        // 0 of 3 completed → stop not complete → isXpClaimable never called
        when(studentProgressRepository.countByStudent_IdAndTask_Stop_IdAndCompletedTrue(
            STUDENT_ID, 1L
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
        when(classroomRepository.existsById(CLASSROOM_ID)).thenReturn(true);
        when(stopRepository.findAllByOrderByOrderIndexAsc()).thenReturn(List.of(first, second));
        when(taskRepository.countByStop_Id(1L)).thenReturn(2L);
        when(taskRepository.countByStop_Id(2L)).thenReturn(3L);
        // 1 of 2 completed → stop 1 not complete → isXpClaimable never called
        when(studentProgressRepository.countByStudent_IdAndTask_Stop_IdAndCompletedTrue(
            STUDENT_ID, 1L
        )).thenReturn(1L);
        // 0 of 3 completed → stop 2 not complete
        when(studentProgressRepository.countByStudent_IdAndTask_Stop_IdAndCompletedTrue(
            STUDENT_ID, 2L
        )).thenReturn(0L);

        var response = gameService.getStops(STUDENT_ID, CLASSROOM_ID);

        assertThat(response.get(0).locked()).isFalse();
        assertThat(response.get(1).locked()).isTrue();
    }

    @Test
    void getStops_zeroTaskPreviousStopDoesNotUnlockNextStop() {
        Stop first = stop(1L, 1, "Nyhetskvartalet");
        Stop second = stop(2L, 2, "Postkontoret");
        when(classroomRepository.existsById(CLASSROOM_ID)).thenReturn(true);
        when(stopRepository.findAllByOrderByOrderIndexAsc()).thenReturn(List.of(first, second));
        // stop 1 has 0 tasks → isStopComplete returns false → isXpClaimable never called
        when(taskRepository.countByStop_Id(1L)).thenReturn(0L);
        when(taskRepository.countByStop_Id(2L)).thenReturn(1L);
        when(studentProgressRepository.countByStudent_IdAndTask_Stop_IdAndCompletedTrue(
            STUDENT_ID, 1L
        )).thenReturn(0L);
        // 0 of 1 completed → stop 2 not complete
        when(studentProgressRepository.countByStudent_IdAndTask_Stop_IdAndCompletedTrue(
            STUDENT_ID, 2L
        )).thenReturn(0L);

        var response = gameService.getStops(STUDENT_ID, CLASSROOM_ID);

        assertThat(response.get(1).locked()).isTrue();
    }

    // ─── submitAnswer — existing tests (fixed) ────────────────────────────────

    @Test
    void submitAnswer_correct_recordsProgress() {
        Stop stop = stop(1L, 1, "Nyhetskvartalet");
        Task task = fakeNewsTask(20L, stop);
        when(taskRepository.findById(20L)).thenReturn(Optional.of(task));
        when(studentProgressRepository.findByStudent_IdAndTask_Id(STUDENT_ID, 20L)).thenReturn(Optional.empty());
        when(userRepository.getReferenceById(STUDENT_ID)).thenReturn(user());
        when(userRepository.findById(STUDENT_ID)).thenReturn(Optional.of(user()));
        when(taskRepository.countByStop_Id(1L)).thenReturn(3L);
        when(studentProgressRepository.countByStudent_IdAndTask_Stop_IdAndCompletedTrue(STUDENT_ID, 1L)).thenReturn(1L);

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
        when(studentProgressRepository.findByStudent_IdAndTask_Id(STUDENT_ID, 20L)).thenReturn(Optional.empty());
        when(userRepository.getReferenceById(STUDENT_ID)).thenReturn(user());
        when(userRepository.findById(STUDENT_ID)).thenReturn(Optional.of(user()));
        when(taskRepository.countByStop_Id(1L)).thenReturn(1L);
        when(studentProgressRepository.countByStudent_IdAndTask_Stop_IdAndCompletedTrue(STUDENT_ID, 1L)).thenReturn(1L);
        when(medalRepository.findByStop_Id(1L)).thenReturn(Optional.of(medal));
        when(studentMedalRepository.existsByStudent_IdAndMedal_Id(STUDENT_ID, 30L)).thenReturn(false);

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
        when(studentProgressRepository.findByStudent_IdAndTask_Id(STUDENT_ID, 20L)).thenReturn(Optional.of(progress));
        when(taskRepository.countByStop_Id(1L)).thenReturn(1L);
        when(studentProgressRepository.countByStudent_IdAndTask_Stop_IdAndCompletedTrue(STUDENT_ID, 1L)).thenReturn(1L);

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
        when(studentProgressRepository.countByStudent_IdAndTask_Stop_IdAndCompletedTrue(STUDENT_ID, 1L)).thenReturn(0L);

        assertThatThrownBy(() -> gameService.getTasks(STUDENT_ID, CLASSROOM_ID, 2L))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Stop is locked");
    }

    @Test
    void getTask_fakeNews_sanitizesAnswerFieldsFromContent() throws Exception {
        Stop stop = stop(1L, 1, "Nyhetskvartalet");
        Task task = fakeNewsTask(20L, stop);
        when(taskRepository.findById(20L)).thenReturn(Optional.of(task));
        when(studentProgressRepository.findByStudent_IdAndTask_Id(STUDENT_ID, 20L)).thenReturn(Optional.empty());

        var response = gameService.getTask(STUDENT_ID, CLASSROOM_ID, 20L);
        var content = response.contentJson();

        assertThat(content.has("explanation")).isFalse();
        assertThat(content.path("articles").get(0).has("isReal")).isFalse();
        assertThat(content.path("articles").get(1).has("isReal")).isFalse();
    }

    @Test
    void getTask_phishingEmail_sanitizesAnswerFieldsFromContent() throws Exception {
        Stop stop = stop(2L, 1, "Postkontoret");
        Task task = phishingTask(21L, stop);
        when(taskRepository.findById(21L)).thenReturn(Optional.of(task));
        when(studentProgressRepository.findByStudent_IdAndTask_Id(STUDENT_ID, 21L)).thenReturn(Optional.empty());

        var response = gameService.getTask(STUDENT_ID, CLASSROOM_ID, 21L);
        var content = response.contentJson();

        assertThat(content.has("explanation")).isFalse();
        assertThat(content.path("email").has("correctAction")).isFalse();
        assertThat(content.path("email").has("suspiciousElements")).isTrue();
    }

    @Test
    void submitAnswer_wrongAnswer_doesNotRecordCompletedProgress() {
        Stop stop = stop(1L, 1, "Nyhetskvartalet");
        Task task = fakeNewsTask(20L, stop);
        when(taskRepository.findById(20L)).thenReturn(Optional.of(task));
        when(studentProgressRepository.findByStudent_IdAndTask_Id(STUDENT_ID, 20L)).thenReturn(Optional.empty());

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
        when(studentProgressRepository.findByStudent_IdAndTask_Id(STUDENT_ID, 20L)).thenReturn(Optional.empty());

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
        when(studentProgressRepository.findByStudent_IdAndTask_Id(STUDENT_ID, 21L)).thenReturn(Optional.empty());
        when(userRepository.getReferenceById(STUDENT_ID)).thenReturn(user());
        when(userRepository.findById(STUDENT_ID)).thenReturn(Optional.of(user()));
        when(taskRepository.countByStop_Id(2L)).thenReturn(2L);
        when(studentProgressRepository.countByStudent_IdAndTask_Stop_IdAndCompletedTrue(STUDENT_ID, 2L)).thenReturn(1L);

        var response = gameService.submitAnswer(
            STUDENT_ID,
            CLASSROOM_ID,
            21L,
            new SubmitAnswerRequest(Map.of("action", "REPORT"))
        );

        assertThat(response.correct()).isTrue();
        verify(studentProgressRepository).save(any(StudentProgress.class));
    }

    @Test
    void submitAnswer_aiPhoto_checksAllImages() {
        Stop stop = stop(3L, 1, "Fotografen");
        Task task = aiPhotoTask(24L, stop);
        when(taskRepository.findById(24L)).thenReturn(Optional.of(task));
        when(studentProgressRepository.findByStudent_IdAndTask_Id(STUDENT_ID, 24L)).thenReturn(Optional.empty());
        when(userRepository.getReferenceById(STUDENT_ID)).thenReturn(user());
        when(userRepository.findById(STUDENT_ID)).thenReturn(Optional.of(user()));
        when(taskRepository.countByStop_Id(3L)).thenReturn(2L);
        when(studentProgressRepository.countByStudent_IdAndTask_Stop_IdAndCompletedTrue(STUDENT_ID, 3L)).thenReturn(1L);

        var response = gameService.submitAnswer(
            STUDENT_ID,
            CLASSROOM_ID,
            24L,
            new SubmitAnswerRequest(Map.of("image_0", "AI_GENERATED", "image_1", "REAL"))
        );

        assertThat(response.correct()).isTrue();
        verify(studentProgressRepository).save(any(StudentProgress.class));
    }

    @Test
    void submitAnswer_socialMedia_checksSelectedOption() {
        Stop stop = stop(6L, 1, "Den sosiale møteplassen");
        Task task = socialMediaActionTask(25L, stop);
        when(taskRepository.findById(25L)).thenReturn(Optional.of(task));
        when(studentProgressRepository.findByStudent_IdAndTask_Id(STUDENT_ID, 25L)).thenReturn(Optional.empty());
        when(userRepository.getReferenceById(STUDENT_ID)).thenReturn(user());
        when(userRepository.findById(STUDENT_ID)).thenReturn(Optional.of(user()));
        when(taskRepository.countByStop_Id(6L)).thenReturn(2L);
        when(studentProgressRepository.countByStudent_IdAndTask_Stop_IdAndCompletedTrue(STUDENT_ID, 6L)).thenReturn(1L);

        var response = gameService.submitAnswer(
            STUDENT_ID,
            CLASSROOM_ID,
            25L,
            new SubmitAnswerRequest(Map.of("selected", "report"))
        );

        assertThat(response.correct()).isTrue();
        verify(studentProgressRepository).save(any(StudentProgress.class));
    }

    @Test
    void submitAnswer_passwordChoice_checksSelectedOption() {
        Stop stop = stop(4L, 1, "Passordbanken");
        Task task = passwordChoiceTask(22L, stop);
        when(taskRepository.findById(22L)).thenReturn(Optional.of(task));
        when(studentProgressRepository.findByStudent_IdAndTask_Id(STUDENT_ID, 22L)).thenReturn(Optional.empty());
        when(userRepository.getReferenceById(STUDENT_ID)).thenReturn(user());
        when(userRepository.findById(STUDENT_ID)).thenReturn(Optional.of(user()));
        when(taskRepository.countByStop_Id(4L)).thenReturn(2L);
        when(studentProgressRepository.countByStudent_IdAndTask_Stop_IdAndCompletedTrue(STUDENT_ID, 4L)).thenReturn(1L);

        var response = gameService.submitAnswer(
            STUDENT_ID,
            CLASSROOM_ID,
            22L,
            new SubmitAnswerRequest(Map.of("selected", "d"))
        );

        assertThat(response.correct()).isTrue();
        verify(studentProgressRepository).save(any(StudentProgress.class));
    }

    @Test
    void submitAnswer_passwordBuilder_requiresStrongPassword() {
        Stop stop = stop(4L, 1, "Passordbanken");
        Task task = passwordBuilderTask(23L, stop);
        when(taskRepository.findById(23L)).thenReturn(Optional.of(task));
        when(studentProgressRepository.findByStudent_IdAndTask_Id(STUDENT_ID, 23L)).thenReturn(Optional.empty());

        var weakResponse = gameService.submitAnswer(
            STUDENT_ID,
            CLASSROOM_ID,
            23L,
            new SubmitAnswerRequest(Map.of("password", "abc"))
        );

        assertThat(weakResponse.correct()).isFalse();
        verify(studentProgressRepository, never()).save(any(StudentProgress.class));
    }

    @Test
    void submitAnswer_passwordBuilder_acceptsStrongPassword() {
        Stop stop = stop(4L, 1, "Passordbanken");
        Task task = passwordBuilderTask(23L, stop);
        when(taskRepository.findById(23L)).thenReturn(Optional.of(task));
        when(studentProgressRepository.findByStudent_IdAndTask_Id(STUDENT_ID, 23L)).thenReturn(Optional.empty());
        when(userRepository.getReferenceById(STUDENT_ID)).thenReturn(user());
        when(userRepository.findById(STUDENT_ID)).thenReturn(Optional.of(user()));
        when(taskRepository.countByStop_Id(4L)).thenReturn(3L);
        when(studentProgressRepository.countByStudent_IdAndTask_Stop_IdAndCompletedTrue(STUDENT_ID, 4L)).thenReturn(1L);

        var strongResponse = gameService.submitAnswer(
            STUDENT_ID,
            CLASSROOM_ID,
            23L,
            new SubmitAnswerRequest(Map.of("password", "Tiger!Måne#42"))
        );

        assertThat(strongResponse.correct()).isTrue();
        verify(studentProgressRepository).save(any(StudentProgress.class));
    }

    // ─── submitAnswer — new tests ─────────────────────────────────────────────

    @Test
    void submitAnswer_awardsOneStarOnFirstCorrectAnswer() {
        Stop stop = stop(1L, 1, "Postkontoret");
        Task task = phishingTask(10L, stop);
        User studentUser = student(STUDENT_ID);

        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(studentProgressRepository.findByStudent_IdAndTask_Id(STUDENT_ID, 10L)).thenReturn(Optional.empty());
        when(taskRepository.countByStop_Id(1L)).thenReturn(3L);
        // Not all tasks done — stop NOT complete
        when(studentProgressRepository.countByStudent_IdAndTask_Stop_IdAndCompletedTrue(STUDENT_ID, 1L)).thenReturn(0L);
        when(userRepository.findById(STUDENT_ID)).thenReturn(Optional.of(studentUser));
        when(userRepository.getReferenceById(STUDENT_ID)).thenReturn(studentUser);

        var result = gameService.submitAnswer(STUDENT_ID, CLASSROOM_ID, 10L, new SubmitAnswerRequest(Map.of("action", "REPORT")));

        assertThat(result.correct()).isTrue();
        assertThat(result.starsEarned()).isEqualTo(1);
        assertThat(result.xpEarned()).isEqualTo(10);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getStarBalance()).isEqualTo(1);
        assertThat(userCaptor.getValue().getXp()).isEqualTo(10);
    }

    @Test
    void submitAnswer_awardsStopBonusXpAndLogsItOnStopCompletion() {
        Stop stop = stop(1L, 1, "Postkontoret");
        Task task = phishingTask(10L, stop);
        User studentUser = student(STUDENT_ID);

        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(studentProgressRepository.findByStudent_IdAndTask_Id(STUDENT_ID, 10L)).thenReturn(Optional.empty());
        when(taskRepository.countByStop_Id(1L)).thenReturn(1L);
        // 1 of 1 tasks complete — stop IS completed
        when(studentProgressRepository.countByStudent_IdAndTask_Stop_IdAndCompletedTrue(STUDENT_ID, 1L)).thenReturn(1L);
        when(userRepository.findById(STUDENT_ID)).thenReturn(Optional.of(studentUser));
        when(userRepository.getReferenceById(STUDENT_ID)).thenReturn(studentUser);
        when(medalRepository.findByStop_Id(1L)).thenReturn(Optional.empty());

        var result = gameService.submitAnswer(STUDENT_ID, CLASSROOM_ID, 10L, new SubmitAnswerRequest(Map.of("action", "REPORT")));

        assertThat(result.correct()).isTrue();
        assertThat(result.stopCompleted()).isTrue();
        // 10 (task XP) + 30 (stop bonus) = 40
        assertThat(result.xpEarned()).isEqualTo(40);
        assertThat(result.starsEarned()).isEqualTo(1);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getXp()).isEqualTo(40);

        verify(studentXpLogRepository).save(any(StudentXpLog.class));
    }

    @Test
    void submitAnswer_doesNotAwardStarsOrXpOnWrongAnswer() {
        Stop stop = stop(1L, 1, "Postkontoret");
        Task task = phishingTask(10L, stop);

        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(studentProgressRepository.findByStudent_IdAndTask_Id(STUDENT_ID, 10L)).thenReturn(Optional.empty());

        var result = gameService.submitAnswer(STUDENT_ID, CLASSROOM_ID, 10L, new SubmitAnswerRequest(Map.of("action", "IGNORE")));

        assertThat(result.correct()).isFalse();
        assertThat(result.starsEarned()).isEqualTo(0);
        assertThat(result.xpEarned()).isEqualTo(0);
        verify(userRepository, never()).save(any());
    }

    // ─── claimWeeklyXp ────────────────────────────────────────────────────────

    @Test
    void claimWeeklyXp_awardsXpIfLastClaimOlderThan7Days() {
        Stop stop = stop(1L, 1, "Postkontoret");
        User studentUser = student(STUDENT_ID);
        StudentXpLog oldLog = new StudentXpLog();
        oldLog.setAwardedAt(LocalDateTime.now().minusDays(8));

        when(stopRepository.findById(1L)).thenReturn(Optional.of(stop));
        when(taskRepository.countByStop_Id(1L)).thenReturn(2L);
        when(studentProgressRepository.countByStudent_IdAndTask_Stop_IdAndCompletedTrue(STUDENT_ID, 1L)).thenReturn(2L);
        when(studentXpLogRepository.findTopByStudent_IdAndStop_IdOrderByAwardedAtDesc(STUDENT_ID, 1L))
            .thenReturn(Optional.of(oldLog));
        when(userRepository.findById(STUDENT_ID)).thenReturn(Optional.of(studentUser));

        ClaimXpResponse response = gameService.claimWeeklyXp(STUDENT_ID, 1L);

        // 10 * 2 tasks + 30 = 50
        assertThat(response.xpEarned()).isEqualTo(50);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getXp()).isEqualTo(50);

        verify(studentXpLogRepository).save(any(StudentXpLog.class));
    }

    @Test
    void claimWeeklyXp_throwsConflictIfClaimedTooRecently() {
        Stop stop = stop(1L, 1, "Postkontoret");
        StudentXpLog recentLog = new StudentXpLog();
        recentLog.setAwardedAt(LocalDateTime.now().minusDays(1));

        when(stopRepository.findById(1L)).thenReturn(Optional.of(stop));
        when(taskRepository.countByStop_Id(1L)).thenReturn(2L);
        when(studentProgressRepository.countByStudent_IdAndTask_Stop_IdAndCompletedTrue(STUDENT_ID, 1L)).thenReturn(2L);
        when(studentXpLogRepository.findTopByStudent_IdAndStop_IdOrderByAwardedAtDesc(STUDENT_ID, 1L))
            .thenReturn(Optional.of(recentLog));

        assertThatThrownBy(() -> gameService.claimWeeklyXp(STUDENT_ID, 1L))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("7 days");
    }

    @Test
    void claimWeeklyXp_throwsConflictIfStopNotCompleted() {
        Stop stop = stop(1L, 1, "Postkontoret");

        when(stopRepository.findById(1L)).thenReturn(Optional.of(stop));
        when(taskRepository.countByStop_Id(1L)).thenReturn(2L);
        when(studentProgressRepository.countByStudent_IdAndTask_Stop_IdAndCompletedTrue(STUDENT_ID, 1L)).thenReturn(0L);

        assertThatThrownBy(() -> gameService.claimWeeklyXp(STUDENT_ID, 1L))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("not yet completed");
    }

    // ─── getProfile ───────────────────────────────────────────────────────────

    @Test
    void getProfile_returnsCorrectLevelXpAndStarBalance() {
        User studentUser = student(STUDENT_ID);
        studentUser.setXp(120);
        studentUser.setStarBalance(9);

        when(userRepository.findById(STUDENT_ID)).thenReturn(Optional.of(studentUser));
        when(studentProgressRepository.countDistinctCompletedStops(STUDENT_ID)).thenReturn(3L);

        PlayerProfileDto profile = gameService.getProfile(STUDENT_ID);

        assertThat(profile.level()).isEqualTo(3);
        assertThat(profile.xp()).isEqualTo(120);
        assertThat(profile.starBalance()).isEqualTo(9);
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

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

    private Task aiPhotoTask(Long id, Stop stop) {
        Task task = task(id, stop, TaskType.AI_PHOTO);
        task.setContentJson("""
            {
              "images": [
                { "id": "image_0", "src": "", "alt": "Rare fingre", "label": "Bilde A" },
                { "id": "image_1", "src": "", "alt": "Normalt mobilbilde", "label": "Bilde B" }
              ],
              "question": "Sorter hvert bilde",
              "explanation": "Det første er KI-generert."
            }
            """);
        task.setCorrectAnswerJson("""
            { "image_0": "AI_GENERATED", "image_1": "REAL" }
            """);
        return task;
    }

    private Task socialMediaActionTask(Long id, Stop stop) {
        Task task = task(id, stop, TaskType.SOCIAL_MEDIA);
        task.setContentJson("""
            {
              "post": {
                "platform": "Fjesbok",
                "username": "BesteFriend99",
                "avatar": "👤",
                "content": "Hei! Jeg vant en premie og trenger telefonnummeret ditt for å sende den."
              },
              "question": "Hva bør du gjøre?",
              "options": [
                { "id": "reply",  "text": "Svar med telefonnummeret mitt" },
                { "id": "ignore", "text": "Ignorer meldingen" },
                { "id": "report", "text": "Rapporter og blokker kontoen" },
                { "id": "ask",    "text": "Spør hvem det er" }
              ],
              "explanation": "Rapporter og blokker kontoen."
            }
            """);
        task.setCorrectAnswerJson("""
            { "selected": "report" }
            }
            """);
        return task;
    }

    private Task passwordChoiceTask(Long id, Stop stop) {
        Task task = task(id, stop, TaskType.PASSWORD);
        task.setContentJson("""
            {
              "type": "CHOICE",
              "question": "Hvilket passord er tryggest?",
              "options": [
                { "id": "a", "value": "Ola123" },
                { "id": "b", "value": "Emma2014" },
                { "id": "c", "value": "Katt" },
                { "id": "d", "value": "F!sk3Taco#92" }
              ],
              "explanation": "F!sk3Taco#92 er sterkest."
            }
            """);
        task.setCorrectAnswerJson("""
            { "selected": "d" }
            """);
        return task;
    }

    private Task passwordBuilderTask(Long id, Stop stop) {
        Task task = task(id, stop, TaskType.PASSWORD);
        task.setContentJson("""
            {
              "type": "BUILDER",
              "question": "Bygg et sterkt passord",
              "words": ["Tiger", "Måne", "Pizza"],
              "symbols": ["!", "#", "@"],
              "numbers": ["7", "42", "99"],
              "minStrength": "STRONG",
              "explanation": "Et sterkt passord er langt og blander tegn."
            }
            """);
        task.setCorrectAnswerJson("""
            { "minStrength": "STRONG" }
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

    private User student(Long id) {
        User u = new User();
        u.setId(id);
        u.setEmail("student" + id + "@student.local");
        u.setPasswordHash("hash");
        u.setRole(User.Role.STUDENT);
        return u;
    }
}
