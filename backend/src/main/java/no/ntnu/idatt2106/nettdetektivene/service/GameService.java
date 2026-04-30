package no.ntnu.idatt2106.nettdetektivene.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import no.ntnu.idatt2106.nettdetektivene.dto.game.ClaimXpResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.game.MedalDto;
import no.ntnu.idatt2106.nettdetektivene.dto.game.PhishingClueFeedbackDto;
import no.ntnu.idatt2106.nettdetektivene.dto.game.PlayerProfileDto;
import no.ntnu.idatt2106.nettdetektivene.dto.game.ProgressResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.game.StopMetaResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.game.StopResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.game.SubmitAnswerRequest;
import no.ntnu.idatt2106.nettdetektivene.dto.game.SubmitAnswerResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.game.TaskResponse;
import no.ntnu.idatt2106.nettdetektivene.entity.Medal;
import no.ntnu.idatt2106.nettdetektivene.entity.StudentMedal;
import no.ntnu.idatt2106.nettdetektivene.entity.StudentProgress;
import no.ntnu.idatt2106.nettdetektivene.entity.StudentXpLog;
import no.ntnu.idatt2106.nettdetektivene.entity.Stop;
import no.ntnu.idatt2106.nettdetektivene.entity.Task;
import no.ntnu.idatt2106.nettdetektivene.entity.TaskType;
import no.ntnu.idatt2106.nettdetektivene.entity.User;
import no.ntnu.idatt2106.nettdetektivene.exception.ResourceNotFoundException;
import no.ntnu.idatt2106.nettdetektivene.repository.ClassroomRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.MedalRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.StopRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.StudentMedalRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.StudentProgressRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.StudentXpLogRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.TaskRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import no.ntnu.idatt2106.nettdetektivene.service.AvatarService;
import no.ntnu.idatt2106.nettdetektivene.service.answer.TaskAnswerChecker;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GameService {

    private static final Logger log = LoggerFactory.getLogger(GameService.class);
    private static final int CORRECT_SCORE = 100;
    private static final int XP_PER_TASK = 10;
    private static final int XP_PER_STOP = 30;
    private static final int SUSPECT_REVEAL_STOP_ORDER = 6;
    private static final EnumSet<TaskType> LEARN_EXCLUDED_TASK_TYPES = EnumSet.of(TaskType.LEARN);
    private static final Set<TaskType> COMPLETION_EXCLUDED_TASK_TYPES = EnumSet.of(
        TaskType.LEARN,
        TaskType.CLUE_RIDDLE
    );

    private final StopRepository stopRepository;
    private final TaskRepository taskRepository;
    private final StudentProgressRepository studentProgressRepository;
    private final MedalRepository medalRepository;
    private final StudentMedalRepository studentMedalRepository;
    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;
    private final ObjectMapper objectMapper;
    private final NotebookService notebookService;
    private final StudentXpLogRepository studentXpLogRepository;
    private final Map<TaskType, TaskAnswerChecker> answerCheckers;
    private final AvatarService avatarService;

    @Value("${app.bypass-stop-lock:false}")
    private boolean bypassStopLock;

    public GameService(
        StopRepository stopRepository,
        TaskRepository taskRepository,
        StudentProgressRepository studentProgressRepository,
        MedalRepository medalRepository,
        StudentMedalRepository studentMedalRepository,
        UserRepository userRepository,
        ClassroomRepository classroomRepository,
        ObjectMapper objectMapper,
        NotebookService notebookService,
        StudentXpLogRepository studentXpLogRepository,
        AvatarService avatarService,
        List<TaskAnswerChecker> answerCheckers
    ) {
        this.stopRepository = stopRepository;
        this.taskRepository = taskRepository;
        this.studentProgressRepository = studentProgressRepository;
        this.medalRepository = medalRepository;
        this.studentMedalRepository = studentMedalRepository;
        this.userRepository = userRepository;
        this.classroomRepository = classroomRepository;
        this.objectMapper = objectMapper;
        this.notebookService = notebookService;
        this.studentXpLogRepository = studentXpLogRepository;
        this.avatarService = avatarService;
        this.answerCheckers = answerCheckers.stream()
            .collect(Collectors.toUnmodifiableMap(TaskAnswerChecker::supportedTaskType, Function.identity()));
    }

    @Transactional(readOnly = true)
    public List<StopMetaResponse> getStopsMeta() {
        log.info("[GameService] getStopsMeta");
        return stopRepository.findAllByOrderByOrderIndexAsc().stream()
            .map(s -> new StopMetaResponse(
                s.getId(),
                s.getName(),
                s.getOrderIndex(),
                s.getTheme(),
                Math.toIntExact(taskRepository.countByStop_IdAndTaskTypeNotIn(s.getId(), LEARN_EXCLUDED_TASK_TYPES))
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<StopResponse> getStops(Long studentId, Long classroomId) {
        log.info("[GameService] getStops studentId={} classroomId={}", studentId, classroomId);
        requireClassroomExists(classroomId);
        return stopRepository.findAllByOrderByOrderIndexAsc().stream()
            .map(stop -> toStopResponse(studentId, classroomId, stop))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasks(Long studentId, Long classroomId, Long stopId) {
        log.info(
            "[GameService] getTasks studentId={} classroomId={} stopId={}",
            studentId,
            classroomId,
            stopId
        );
        Stop stop = stopRepository.findById(stopId)
            .orElseThrow(() -> {
                log.warn("[GameService] stop not found stopId={} studentId={} classroomId={}", stopId, studentId, classroomId);
                return new ResourceNotFoundException("Stop not found");
            });
        requireUnlocked(studentId, classroomId, stop);

        return taskRepository.findByStop_IdOrderByOrderIndexAscIdAsc(stopId).stream()
            .map(task -> toTaskResponse(studentId, classroomId, task))
            .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(Long studentId, Long classroomId, Long taskId) {
        log.info(
            "[GameService] getTask studentId={} classroomId={} taskId={}",
            studentId,
            classroomId,
            taskId
        );
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> {
                log.warn("[GameService] task not found taskId={} studentId={} classroomId={}", taskId, studentId, classroomId);
                return new ResourceNotFoundException("Task not found");
            });
        requireUnlocked(studentId, classroomId, task.getStop());
        requireTaskSequenceAvailable(studentId, task);
        return toTaskResponse(studentId, classroomId, task);
    }

    @Transactional
    public SubmitAnswerResponse submitAnswer(
        Long studentId,
        Long classroomId,
        Long taskId,
        SubmitAnswerRequest req
    ) {
        log.info(
            "[GameService] submitAnswer studentId={} classroomId={} taskId={}",
            studentId,
            classroomId,
            taskId
        );
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> {
                log.warn("[GameService] task not found taskId={} studentId={} classroomId={}", taskId, studentId, classroomId);
                return new ResourceNotFoundException("Task not found");
            });
        requireUnlocked(studentId, classroomId, task.getStop());
        requireTaskSequenceAvailable(studentId, task);

        String explanation = extractExplanation(task);
        Optional<StudentProgress> existingProgress = studentProgressRepository
            .findByStudent_IdAndTask_Id(studentId, taskId);

        if (existingProgress.map(StudentProgress::isCompleted).orElse(false)) {
            if (task.getTaskType() == TaskType.CLUE_RIDDLE && !checkAnswer(task, req == null ? null : req.answer())) {
                log.info("[GameService] wrong answer on already completed task studentId={} taskId={}", studentId, taskId);
                List<String> correctClueIds = task.getTaskType() == TaskType.PHISHING_EMAIL
                    ? correctClueIdsFor(task) : List.of();
                List<PhishingClueFeedbackDto> phishingClues = task.getTaskType() == TaskType.PHISHING_EMAIL
                    ? phishingCluesFor(task) : List.of();
                Integer correctArticleIndex = task.getTaskType() == TaskType.FAKE_NEWS
                    ? fakeNewsCorrectIndex(task) : null;
                return new SubmitAnswerResponse(false, 0, explanation, false, null, 0, 0, correctClueIds, phishingClues, correctArticleIndex, null, false);
            }
            boolean stopCompleted = canReturnStopCompletion(task) && isStopComplete(studentId, task.getStop().getId());
            String clueText = stopCompleted ? task.getStop().getClueText() : null;
            List<String> correctClueIds = task.getTaskType() == TaskType.PHISHING_EMAIL
                ? correctClueIdsFor(task) : List.of();
            List<PhishingClueFeedbackDto> phishingClues = task.getTaskType() == TaskType.PHISHING_EMAIL
                ? phishingCluesFor(task) : List.of();
            boolean currentAnswerCorrect = checkAnswer(task, req == null ? null : req.answer());
            return new SubmitAnswerResponse(
                currentAnswerCorrect,
                existingProgress.get().getScore(),
                explanation,
                stopCompleted,
                null,
                0,
                0,
                correctClueIds,
                phishingClues,
                null,
                clueText,
                false
            );
        }

        if (!checkAnswer(task, req == null ? null : req.answer())) {
            log.info("[GameService] wrong answer studentId={} taskId={}", studentId, taskId);
            List<String> correctClueIds = task.getTaskType() == TaskType.PHISHING_EMAIL
                ? correctClueIdsFor(task) : List.of();
            List<PhishingClueFeedbackDto> phishingClues = task.getTaskType() == TaskType.PHISHING_EMAIL
                ? phishingCluesFor(task) : List.of();
            Integer correctArticleIndex = task.getTaskType() == TaskType.FAKE_NEWS
                ? fakeNewsCorrectIndex(task) : null;
            return new SubmitAnswerResponse(false, 0, explanation, false, null, 0, 0, correctClueIds, phishingClues, correctArticleIndex, null, false);
        }

        StudentProgress progress = existingProgress.orElseGet(StudentProgress::new);
        progress.setStudent(userRepository.getReferenceById(studentId));
        progress.setStop(task.getStop());
        progress.setTask(task);
        progress.setCompleted(true);
        progress.setScore(CORRECT_SCORE);
        progress.setAttempts(progress.getAttempts() + 1);
        progress.setCompletedAt(LocalDateTime.now());
        studentProgressRepository.save(progress);

        boolean earnsTaskReward = task.getTaskType() != TaskType.LEARN;
        User student = earnsTaskReward
            ? userRepository.findById(studentId).orElseThrow(() -> new ResourceNotFoundException("User not found"))
            : null;
        if (earnsTaskReward) {
            student.setStarBalance(student.getStarBalance() + 1);
            student.setXp(student.getXp() + XP_PER_TASK);
        }

        boolean stopCompleted = canReturnStopCompletion(task) && isStopComplete(studentId, task.getStop().getId());
        boolean awardStopCompletion = canCompleteStop(task) && stopCompleted;
        int xpEarned = earnsTaskReward ? XP_PER_TASK : 0;
        String clueText = stopCompleted ? task.getStop().getClueText() : null;
        boolean showSuspectReveal = stopCompleted
            && task.getTaskType() == TaskType.CLUE_RIDDLE
            && shouldShowSuspectReveal(task.getStop());

        if (awardStopCompletion) {
            log.info("[GameService] stop completed studentId={} stopId={}", studentId, task.getStop().getId());
            student.setXp(student.getXp() + XP_PER_STOP);
            xpEarned += XP_PER_STOP;
            int taskCount = Math.toIntExact(requiredTaskCount(task.getStop().getId()));
            StudentXpLog xpLog = new StudentXpLog();
            xpLog.setStudent(student);
            xpLog.setStop(task.getStop());
            xpLog.setXpAmount(XP_PER_TASK * taskCount + XP_PER_STOP);
            xpLog.setAwardedAt(LocalDateTime.now());
            studentXpLogRepository.save(xpLog);
        }
        if (shouldStoreClue(task, stopCompleted, awardStopCompletion)) {
            notebookService.createAutoClueIfNotExists(studentId, task.getStop());
        }
        if (student != null) {
            userRepository.save(student);
        }
        int starsEarned = earnsTaskReward ? 1 : 0;
        log.info("[GameService] awarded starsEarned={} xpEarned={} studentId={} taskId={}", starsEarned, xpEarned, studentId, taskId);

        MedalDto medalEarned = awardStopCompletion
            ? checkAndAwardMedal(studentId, task.getStop().getId()).map(this::toMedalDto).orElse(null)
            : null;

        List<String> correctClueIds = task.getTaskType() == TaskType.PHISHING_EMAIL
            ? correctClueIdsFor(task) : List.of();
        List<PhishingClueFeedbackDto> phishingClues = task.getTaskType() == TaskType.PHISHING_EMAIL
            ? phishingCluesFor(task) : List.of();

        return new SubmitAnswerResponse(
            true,
            CORRECT_SCORE,
            explanation,
            stopCompleted,
            medalEarned,
            starsEarned,
            xpEarned,
            correctClueIds,
            phishingClues,
            null,
            clueText,
            showSuspectReveal
        );
    }

    @Transactional(readOnly = true)
    public ProgressResponse getProgress(Long studentId, Long classroomId) {
        log.info("[GameService] getProgress studentId={} classroomId={}", studentId, classroomId);
        List<StopResponse> stops = getStops(studentId, classroomId);
        int stopsCompleted = (int) stops.stream().filter(StopResponse::completed).count();
        return new ProgressResponse(stopsCompleted, stops.size(), stops);
    }

    @Transactional(readOnly = true)
    public PlayerProfileDto getProfile(Long studentId) {
        log.info("[GameService] getProfile studentId={}", studentId);
        User student = userRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        int level = (int) studentProgressRepository.countDistinctCompletedStops(studentId);
        log.info("[GameService] getProfile studentId={} level={} xp={} stars={}", studentId, level, student.getXp(), student.getStarBalance());
        return new PlayerProfileDto(level, student.getXp(), student.getStarBalance());
    }

    @Transactional
    public ClaimXpResponse claimWeeklyXp(Long studentId, Long stopId) {
        log.info("[GameService] claimWeeklyXp studentId={} stopId={}", studentId, stopId);
        Stop stop = stopRepository.findById(stopId)
            .orElseThrow(() -> {
                log.warn("[GameService] claimWeeklyXp stop not found stopId={}", stopId);
                return new ResourceNotFoundException("Stop not found");
            });

        if (!isStopComplete(studentId, stopId)) {
            log.warn("[GameService] claimWeeklyXp stop not completed studentId={} stopId={}", studentId, stopId);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Stop not yet completed");
        }

        Optional<StudentXpLog> lastLog = studentXpLogRepository
            .findTopByStudent_IdAndStop_IdOrderByAwardedAtDesc(studentId, stopId);
        if (lastLog.isPresent() && lastLog.get().getAwardedAt().isAfter(LocalDateTime.now().minusDays(7))) {
            log.warn("[GameService] claimWeeklyXp too recent studentId={} stopId={} lastAt={}", studentId, stopId, lastLog.get().getAwardedAt());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "XP already claimed for this stop within the last 7 days");
        }

        int taskCount = Math.toIntExact(requiredTaskCount(stopId));
        int xpEarned = XP_PER_TASK * taskCount + XP_PER_STOP;

        User student = userRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        student.setXp(student.getXp() + xpEarned);
        userRepository.save(student);

        StudentXpLog xpLog = new StudentXpLog();
        xpLog.setStudent(student);
        xpLog.setStop(stop);
        xpLog.setXpAmount(xpEarned);
        xpLog.setAwardedAt(LocalDateTime.now());
        studentXpLogRepository.save(xpLog);

        log.info("[GameService] claimWeeklyXp awarded xpEarned={} studentId={} stopId={}", xpEarned, studentId, stopId);
        return new ClaimXpResponse(xpEarned);
    }

    private boolean isStopUnlocked(Long studentId, Long classroomId, Stop stop) {
        if (bypassStopLock) {
            log.debug("[GameService] bypassStopLock active — stop {} unlocked unconditionally", stop.getId());
            return true;
        }
        if (stop.getOrderIndex() <= 1) {
            return true;
        }

        return stopRepository.findAllByOrderByOrderIndexAsc().stream()
            .filter(candidate -> candidate.getOrderIndex().equals(stop.getOrderIndex() - 1))
            .findFirst()
            .map(previous -> allTasksCompleted(studentId, previous.getId()))
            .orElse(false);
    }

    private boolean isStopComplete(Long studentId, Long stopId) {
        long taskCount = requiredTaskCount(stopId);
        return taskCount > 0 && completedTaskCount(studentId, stopId) >= taskCount;
    }

    private Optional<Medal> checkAndAwardMedal(Long studentId, Long stopId) {
        Optional<Medal> medal = medalRepository.findByStop_Id(stopId);
        if (medal.isEmpty()) {
            return Optional.empty();
        }

        Long medalId = medal.get().getId();
        if (studentMedalRepository.existsByStudent_IdAndMedal_Id(studentId, medalId)) {
            return Optional.empty();
        }

        StudentMedal studentMedal = new StudentMedal();
        studentMedal.setStudent(userRepository.getReferenceById(studentId));
        studentMedal.setMedal(medal.get());
        studentMedalRepository.save(studentMedal);
        log.info("[GameService] awarded medal studentId={} stopId={} medalId={}", studentId, stopId, medal.get().getId());
        try {
            avatarService.handleMedalUnlock(studentId, stopId);
        } catch (Exception e) {
            log.error("[GameService] handleMedalUnlock failed for studentId={} stopId={}, avatar reward skipped", studentId, stopId, e);
        }
        return medal;
    }

    private boolean checkAnswer(Task task, Map<String, Object> answer) {
        if (answer == null || task.getCorrectAnswerJson() == null) {
            return false;
        }

        try {
            JsonNode correctAnswer = objectMapper.readTree(task.getCorrectAnswerJson());
            Map<String, Object> normalizedAnswer = normalizeAnswerForChecker(task.getTaskType(), correctAnswer, answer);
            if (task.getTaskType() == TaskType.FINAL_BOSS) {
                return checkFinalBossAnswer(correctAnswer, normalizedAnswer);
            }
            TaskAnswerChecker checker = answerCheckers.get(task.getTaskType());
            if (checker == null) {
                log.warn("[GameService] no answer checker registered for taskType={}", task.getTaskType());
                return false;
            }
            return checker.isCorrect(task, correctAnswer, normalizedAnswer);
        } catch (JsonProcessingException exception) {
            log.error("[GameService] failed to parse correct answer JSON taskId={}", task.getId(), exception);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Task answer data is invalid");
        }
    }

    private Map<String, Object> normalizeAnswerForChecker(
        TaskType taskType,
        JsonNode correctAnswer,
        Map<String, Object> answer
    ) {
        if (taskType != TaskType.SOCIAL_MEDIA) {
            return answer;
        }

        Object selected = answer.get("selected");
        Object action = answer.get("action");

        if ((hasSingleOrMultiple(correctAnswer, "selected", "acceptedSelected")) && selected == null && action != null) {
            Map<String, Object> normalized = new java.util.HashMap<>(answer);
            normalized.put("selected", action);
            return normalized;
        }

        if ((hasSingleOrMultiple(correctAnswer, "action", "acceptedActions")) && action == null && selected != null) {
            Map<String, Object> normalized = new java.util.HashMap<>(answer);
            normalized.put("action", selected);
            return normalized;
        }

        return answer;
    }

    private boolean hasSingleOrMultiple(JsonNode correctAnswer, String singleKey, String multipleKey) {
        return !correctAnswer.path(singleKey).isMissingNode() || correctAnswer.path(multipleKey).isArray();
    }

    private boolean checkFakeNewsAnswer(JsonNode correctAnswer, Map<String, Object> answer) {
        Iterator<Map.Entry<String, JsonNode>> fields = correctAnswer.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            Boolean submitted = asBoolean(answer.get(field.getKey()));
            if (submitted == null || submitted != field.getValue().asBoolean()) {
                return false;
            }
        }
        return true;
    }

    private boolean checkPhishingEmailAnswer(JsonNode correctAnswer, Map<String, Object> answer) {
        return PhishingAnswerChecker.check(correctAnswer, answer);
    }

    private boolean checkFinalBossAnswer(JsonNode correctAnswer, Map<String, Object> answer) {
        for (int i = 0; i < 6; i++) {
            String key = "challenge_" + i;
            JsonNode challengeCorrect = correctAnswer.path(key);
            if (challengeCorrect.isMissingNode()) {
                log.warn("[GameService] FINAL_BOSS correctAnswer missing key: {}", key);
                return false;
            }
            Object raw = answer.get(key);
            if (raw == null) {
                log.warn("[GameService] FINAL_BOSS submitted answer missing key: {}", key);
                return false;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> challengeAnswer = (Map<String, Object>) raw;
            if (!checkChallengeAnswer(challengeCorrect, challengeAnswer)) {
                log.info("[GameService] FINAL_BOSS challenge {} incorrect", i);
                return false;
            }
        }
        return true;
    }

    private boolean checkChallengeAnswer(JsonNode correct, Map<String, Object> answer) {
        if (!correct.path("action").isMissingNode()) {
            return checkPhishingEmailAnswer(correct, answer);
        }
        if (!correct.path("selected").isMissingNode()) {
            Object submitted = answer.get("selected");
            if (submitted == null) return false;
            return correct.path("selected").asText().equalsIgnoreCase(String.valueOf(submitted));
        }
        if (correct.fieldNames().hasNext()) {
            String firstKey = correct.fieldNames().next();
            if (firstKey.startsWith("article_")) return checkFakeNewsAnswer(correct, answer);
            if (firstKey.startsWith("image_"))   return checkAiPhotoAnswer(correct, answer);
        }
        log.warn("[GameService] checkChallengeAnswer: unrecognised correct answer shape");
        return false;
    }

    private boolean checkAiPhotoAnswer(JsonNode correctAnswer, Map<String, Object> answer) {
        Iterator<Map.Entry<String, JsonNode>> fields = correctAnswer.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            Object submitted = answer.get(field.getKey());
            if (submitted == null) return false;
            if (!field.getValue().asText().equalsIgnoreCase(String.valueOf(submitted))) return false;
        }
        return true;
    }

    private Integer fakeNewsCorrectIndex(Task task) {
        try {
            JsonNode correct = objectMapper.readTree(task.getCorrectAnswerJson());
            Iterator<Map.Entry<String, JsonNode>> fields = correct.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                if (!entry.getValue().asBoolean()) {
                    return Integer.parseInt(entry.getKey().replace("article_", ""));
                }
            }
        } catch (Exception e) {
            log.warn("[GameService] Failed to parse correctAnswerJson for fake news index taskId={}", task.getId());
        }
        return null;
    }

    private List<String> correctClueIdsFor(Task task) {
        try {
            JsonNode correct = objectMapper.readTree(task.getCorrectAnswerJson());
            return PhishingAnswerChecker.requiredClueIds(correct);
        } catch (Exception e) {
            log.warn("[GameService] Failed to parse correctAnswerJson for clue IDs taskId={}", task.getId());
            return List.of();
        }
    }

    private List<PhishingClueFeedbackDto> phishingCluesFor(Task task) {
        try {
            JsonNode clues = objectMapper.readTree(task.getContentJson()).path("email").path("clues");
            if (!clues.isArray()) {
                return List.of();
            }

            List<PhishingClueFeedbackDto> feedback = new ArrayList<>();
            clues.forEach(clue -> {
                if (!clue.isObject()) return;
                feedback.add(new PhishingClueFeedbackDto(
                    clue.path("id").asText(),
                    clue.path("label").asText(),
                    clue.path("explanation").asText(""),
                    clue.path("isClue").asBoolean(false)
                ));
            });
            return feedback;
        } catch (JsonProcessingException e) {
            log.error("[GameService] Failed to parse contentJson for phishing clue feedback taskId={}", task.getId(), e);
            return List.of();
        }
    }

    private Boolean asBoolean(Object value) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (value instanceof String stringValue) {
            if ("true".equalsIgnoreCase(stringValue)) {
                return true;
            }
            if ("false".equalsIgnoreCase(stringValue)) {
                return false;
            }
        }
        return null;
    }

    private StopResponse toStopResponse(Long studentId, Long classroomId, Stop stop) {
        int taskCount = Math.toIntExact(requiredTaskCount(stop.getId()));
        int correctCount = Math.toIntExact(completedTaskCount(studentId, stop.getId()));
        boolean unlocked = isStopUnlocked(studentId, classroomId, stop);
        boolean completed = isStopComplete(studentId, stop.getId());
        boolean xpClaimable = completed && isXpClaimable(studentId, stop.getId());
        return new StopResponse(
            stop.getId(),
            stop.getName(),
            stop.getOrderIndex(),
            stop.getDescription(),
            !unlocked,
            completed,
            taskCount,
            correctCount,
            xpClaimable,
            stop.getTheme()
        );
    }

    private boolean isXpClaimable(Long studentId, Long stopId) {
        return studentXpLogRepository
            .findTopByStudent_IdAndStop_IdOrderByAwardedAtDesc(studentId, stopId)
            .map(logEntry -> logEntry.getAwardedAt().isBefore(LocalDateTime.now().minusDays(7)))
            .orElse(false);
    }

    private TaskResponse toTaskResponse(Long studentId, Long classroomId, Task task) {
        boolean alreadyCompleted = studentProgressRepository
            .findByStudent_IdAndTask_Id(studentId, task.getId())
            .map(StudentProgress::isCompleted)
            .orElse(false);

        Stop stop = task.getStop();
        return new TaskResponse(
            task.getId(),
            stop.getId(),
            stop.getName(),
            stop.getDescription(),
            stop.getOrderIndex(),
            stop.getTheme(),
            task.getTaskType().name(),
            sanitizeContentForClient(task.getTaskType(), task.getContentJson(), task.getCorrectAnswerJson()),
            task.getGuidanceText(),
            alreadyCompleted
        );
    }

    private MedalDto toMedalDto(Medal medal) {
        return new MedalDto(medal.getId(), medal.getName(), medal.getDescription(), medal.getImageUrl());
    }

    private void requireClassroomExists(Long classroomId) {
        if (!classroomRepository.existsById(classroomId)) {
            log.warn("[GameService] classroom not found classroomId={}", classroomId);
            throw new ResourceNotFoundException("Classroom not found");
        }
    }

    private void requireUnlocked(Long studentId, Long classroomId, Stop stop) {
        if (!isStopUnlocked(studentId, classroomId, stop)) {
            log.warn(
                "[GameService] locked stop access studentId={} classroomId={} stopId={}",
                studentId,
                classroomId,
                stop.getId()
            );
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Stop is locked");
        }
    }

    private void requireTaskSequenceAvailable(Long studentId, Task task) {
        if (task.getTaskType() == TaskType.LEARN) {
            return;
        }

        List<Task> stopTasks = taskRepository.findByStop_IdOrderByOrderIndexAscIdAsc(task.getStop().getId());
        boolean tutorialComplete = stopTasks.stream()
            .filter(candidate -> candidate.getTaskType() == TaskType.LEARN)
            .allMatch(candidate -> studentProgressRepository
                .findByStudent_IdAndTask_Id(studentId, candidate.getId())
                .map(StudentProgress::isCompleted)
                .orElse(false));

        if (!tutorialComplete) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tutorial must be completed first");
        }

        boolean previousRequiredTasksComplete = stopTasks.stream()
            .filter(candidate -> !COMPLETION_EXCLUDED_TASK_TYPES.contains(candidate.getTaskType()))
            .filter(candidate -> candidate.getOrderIndex() < task.getOrderIndex())
            .allMatch(candidate -> studentProgressRepository
                .findByStudent_IdAndTask_Id(studentId, candidate.getId())
                .map(StudentProgress::isCompleted)
                .orElse(false));

        if (!previousRequiredTasksComplete) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Previous tasks must be completed first");
        }
    }

    private boolean allTasksCompleted(Long studentId, Long stopId) {
        long taskCount = requiredTaskCount(stopId);
        return taskCount > 0 && completedTaskCount(studentId, stopId) >= taskCount;
    }

    private long completedTaskCount(Long studentId, Long stopId) {
        return studentProgressRepository.countByStudent_IdAndTask_Stop_IdAndCompletedTrueAndTask_TaskTypeNotIn(
            studentId,
            stopId,
            COMPLETION_EXCLUDED_TASK_TYPES
        );
    }

    private long requiredTaskCount(Long stopId) {
        // LEARN and CLUE_RIDDLE are excluded: LEARN is introductory, CLUE_RIDDLE is a bonus clue.
        return taskRepository.countByStop_IdAndTaskTypeNotIn(stopId, COMPLETION_EXCLUDED_TASK_TYPES);
    }

    private boolean shouldShowSuspectReveal(Stop stop) {
        return Integer.valueOf(SUSPECT_REVEAL_STOP_ORDER).equals(stop.getOrderIndex());
    }

    private boolean canCompleteStop(Task task) {
        return !COMPLETION_EXCLUDED_TASK_TYPES.contains(task.getTaskType());
    }

    private boolean canReturnStopCompletion(Task task) {
        return task.getTaskType() != TaskType.LEARN;
    }

    private boolean shouldStoreClue(Task task, boolean stopCompleted, boolean awardStopCompletion) {
        if (!stopCompleted) {
            return false;
        }
        if (task.getTaskType() == TaskType.CLUE_RIDDLE) {
            return true;
        }
        return awardStopCompletion && !taskRepository.existsByStop_IdAndTaskType(task.getStop().getId(), TaskType.CLUE_RIDDLE);
    }

    private String extractExplanation(Task task) {
        try {
            return objectMapper.readTree(task.getContentJson()).path("explanation").asText("");
        } catch (JsonProcessingException exception) {
            log.error("[GameService] failed to parse content JSON taskId={}", task.getId(), exception);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Task content data is invalid");
        }
    }

    private JsonNode sanitizeContentForClient(TaskType type, String contentJson, String correctAnswerJson) {
        try {
            JsonNode parsed = objectMapper.readTree(contentJson);
            if (!parsed.isObject()) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Task content invalid");
            }

            ObjectNode root = (ObjectNode) parsed;
            root.remove("explanation");

            if (type == TaskType.FAKE_NEWS && root.path("articles").isArray()) {
                ArrayNode articles = (ArrayNode) root.path("articles");
                articles.forEach(article -> {
                    if (article.isObject()) {
                        ((ObjectNode) article).remove("isReal");
                    }
                });
            }

            if (type == TaskType.PHISHING_EMAIL && root.path("email").isObject()) {
                ObjectNode email = (ObjectNode) root.path("email");
                email.remove("correctAction");
                if (email.path("clues").isArray()) {
                    ArrayNode clues = (ArrayNode) email.path("clues");
                    clues.forEach(clue -> {
                        if (clue.isObject()) {
                            ((ObjectNode) clue).remove("isClue");
                            ((ObjectNode) clue).remove("explanation");
                        }
                    });
                }
            }

            if (type == TaskType.SOCIAL_MEDIA) {
                root.remove("type");
                shuffleSocialMediaOptions(root, correctAnswerJson);
            }

            return root;
        } catch (JsonProcessingException exception) {
            log.error("[GameService] failed to sanitize content taskType={}", type, exception);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Task content invalid");
        }
    }

    private void shuffleSocialMediaOptions(ObjectNode root, String correctAnswerJson) throws JsonProcessingException {
        if (!root.path("options").isArray()) {
            return;
        }

        ArrayNode options = (ArrayNode) root.path("options");
        List<JsonNode> shuffledOptions = new ArrayList<>();
        options.forEach(option -> shuffledOptions.add(option.deepCopy()));
        Collections.shuffle(shuffledOptions);

        // Only singular-answer tasks can be biased away from the middle slot.
        // Multi-answer tasks intentionally keep a fully random order.
        moveCorrectOptionAwayFromMiddle(shuffledOptions, socialMediaCorrectOptionId(correctAnswerJson));

        ArrayNode shuffledArray = objectMapper.createArrayNode();
        shuffledOptions.forEach(shuffledArray::add);
        root.set("options", shuffledArray);
    }

    private String socialMediaCorrectOptionId(String correctAnswerJson) throws JsonProcessingException {
        JsonNode correctAnswer = objectMapper.readTree(correctAnswerJson);
        if (!correctAnswer.path("selected").isMissingNode()) {
            return correctAnswer.path("selected").asText();
        }
        if (!correctAnswer.path("action").isMissingNode()) {
            return correctAnswer.path("action").asText();
        }
        if (correctAnswer.path("acceptedSelected").isArray() && !correctAnswer.path("acceptedSelected").isEmpty()) {
            return correctAnswer.path("acceptedSelected").get(0).asText();
        }
        if (correctAnswer.path("acceptedActions").isArray() && !correctAnswer.path("acceptedActions").isEmpty()) {
            return correctAnswer.path("acceptedActions").get(0).asText();
        }
        return null;
    }

    private void moveCorrectOptionAwayFromMiddle(List<JsonNode> options, String correctOptionId) {
        if (correctOptionId == null || options.size() < 3) {
            return;
        }

        int correctIndex = -1;
        for (int i = 0; i < options.size(); i++) {
            if (correctOptionId.equalsIgnoreCase(options.get(i).path("id").asText())) {
                correctIndex = i;
                break;
            }
        }

        if (correctIndex < 0 || !isMiddleIndex(correctIndex, options.size())) {
            return;
        }

        List<Integer> edgeIndexes = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            if (i != correctIndex && !isMiddleIndex(i, options.size())) {
                edgeIndexes.add(i);
            }
        }

        if (edgeIndexes.isEmpty()) {
            return;
        }

        Collections.shuffle(edgeIndexes);
        Collections.swap(options, correctIndex, edgeIndexes.getFirst());
    }

    private boolean isMiddleIndex(int index, int size) {
        if (size < 3) {
            return false;
        }
        if (size % 2 == 1) {
            return index == size / 2;
        }
        return index == (size / 2) - 1 || index == size / 2;
    }
}
