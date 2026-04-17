package no.ntnu.idatt2106.nettdetektivene.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import no.ntnu.idatt2106.nettdetektivene.dto.game.MedalDto;
import no.ntnu.idatt2106.nettdetektivene.dto.game.ProgressResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.game.StopResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.game.SubmitAnswerRequest;
import no.ntnu.idatt2106.nettdetektivene.dto.game.SubmitAnswerResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.game.TaskResponse;
import no.ntnu.idatt2106.nettdetektivene.entity.Medal;
import no.ntnu.idatt2106.nettdetektivene.entity.StudentMedal;
import no.ntnu.idatt2106.nettdetektivene.entity.StudentProgress;
import no.ntnu.idatt2106.nettdetektivene.entity.Stop;
import no.ntnu.idatt2106.nettdetektivene.entity.Task;
import no.ntnu.idatt2106.nettdetektivene.entity.TaskType;
import no.ntnu.idatt2106.nettdetektivene.exception.ResourceNotFoundException;
import no.ntnu.idatt2106.nettdetektivene.repository.ClassroomRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.MedalRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.StopRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.StudentMedalRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.StudentProgressRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.TaskRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GameService {

    private static final Logger log = LoggerFactory.getLogger(GameService.class);
    private static final int CORRECT_SCORE = 100;

    private final StopRepository stopRepository;
    private final TaskRepository taskRepository;
    private final StudentProgressRepository studentProgressRepository;
    private final MedalRepository medalRepository;
    private final StudentMedalRepository studentMedalRepository;
    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;
    private final ObjectMapper objectMapper;

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

        return taskRepository.findByStop_IdOrderByIdAsc(stopId).stream()
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

        String explanation = extractExplanation(task);
        Optional<StudentProgress> existingProgress = studentProgressRepository
            .findByStudent_IdAndTask_Id(studentId, taskId);

        if (existingProgress.map(StudentProgress::isCompleted).orElse(false)) {
            return new SubmitAnswerResponse(
                true,
                existingProgress.get().getScore(),
                explanation,
                isStopComplete(studentId, task.getStop().getId()),
                null
            );
        }

        if (!checkAnswer(task, req == null ? null : req.answer())) {
            log.info("[GameService] wrong answer studentId={} taskId={}", studentId, taskId);
            return new SubmitAnswerResponse(false, 0, explanation, false, null);
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

        boolean stopCompleted = isStopComplete(studentId, task.getStop().getId());
        if (stopCompleted) {
            log.info("[GameService] stop completed studentId={} stopId={}", studentId, task.getStop().getId());
            notebookService.createAutoTipIfNotExists(studentId, task.getStop());
        }
        MedalDto medalEarned = stopCompleted
            ? checkAndAwardMedal(studentId, task.getStop().getId()).map(this::toMedalDto).orElse(null)
            : null;

        return new SubmitAnswerResponse(true, CORRECT_SCORE, explanation, stopCompleted, medalEarned);
    }

    @Transactional(readOnly = true)
    public ProgressResponse getProgress(Long studentId, Long classroomId) {
        log.info("[GameService] getProgress studentId={} classroomId={}", studentId, classroomId);
        List<StopResponse> stops = getStops(studentId, classroomId);
        int stopsCompleted = (int) stops.stream().filter(StopResponse::completed).count();
        return new ProgressResponse(stopsCompleted, stops.size(), stops);
    }

    private boolean isStopUnlocked(Long studentId, Long classroomId, Stop stop) {
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
        long taskCount = taskRepository.countByStop_Id(stopId);
        return taskCount > 0 && completedTaskCount(studentId, stopId) == taskCount;
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
        return medal;
    }

    private boolean checkAnswer(Task task, Map<String, Object> answer) {
        if (answer == null || task.getCorrectAnswerJson() == null) {
            return false;
        }

        try {
            JsonNode correctAnswer = objectMapper.readTree(task.getCorrectAnswerJson());
            if (task.getTaskType() == TaskType.FAKE_NEWS) {
                return checkFakeNewsAnswer(correctAnswer, answer);
            }
            if (task.getTaskType() == TaskType.PHISHING_EMAIL) {
                return checkPhishingEmailAnswer(correctAnswer, answer);
            }
            return false;
        } catch (JsonProcessingException exception) {
            log.error("[GameService] failed to parse correct answer JSON taskId={}", task.getId(), exception);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Task answer data is invalid");
        }
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
        Object submittedAction = answer.get("action");
        if (submittedAction == null || correctAnswer.path("action").isMissingNode()) {
            return false;
        }
        return correctAnswer.path("action").asText().equalsIgnoreCase(String.valueOf(submittedAction));
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
        int taskCount = Math.toIntExact(taskRepository.countByStop_Id(stop.getId()));
        boolean unlocked = isStopUnlocked(studentId, classroomId, stop);
        return new StopResponse(
            stop.getId(),
            stop.getName(),
            stop.getOrderIndex(),
            stop.getDescription(),
            !unlocked,
            isStopComplete(studentId, stop.getId()),
            taskCount
        );
    }

    private TaskResponse toTaskResponse(Long studentId, Long classroomId, Task task) {
        boolean alreadyCompleted = studentProgressRepository
            .findByStudent_IdAndTask_Id(studentId, task.getId())
            .map(StudentProgress::isCompleted)
            .orElse(false);

        return new TaskResponse(
            task.getId(),
            task.getStop().getId(),
            task.getTaskType().name(),
            sanitizeContentForClient(task.getTaskType(), task.getContentJson()),
            task.getGuidanceText(),
            alreadyCompleted
        );
    }

    private MedalDto toMedalDto(Medal medal) {
        return new MedalDto(medal.getId(), medal.getName(), medal.getDescription());
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

    private boolean allTasksCompleted(Long studentId, Long stopId) {
        long taskCount = taskRepository.countByStop_Id(stopId);
        return taskCount > 0 && completedTaskCount(studentId, stopId) == taskCount;
    }

    private long completedTaskCount(Long studentId, Long stopId) {
        return studentProgressRepository.countByStudent_IdAndTask_Stop_IdAndCompletedTrue(studentId, stopId);
    }

    private String extractExplanation(Task task) {
        try {
            return objectMapper.readTree(task.getContentJson()).path("explanation").asText("");
        } catch (JsonProcessingException exception) {
            log.error("[GameService] failed to parse content JSON taskId={}", task.getId(), exception);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Task content data is invalid");
        }
    }

    private JsonNode sanitizeContentForClient(TaskType type, String contentJson) {
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
            }

            return root;
        } catch (JsonProcessingException exception) {
            log.error("[GameService] failed to sanitize content taskType={}", type, exception);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Task content invalid");
        }
    }
}
