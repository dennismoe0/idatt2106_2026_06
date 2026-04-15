package no.ntnu.idatt2106.nettdetektivene.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        return stopRepository.findAllByOrderByOrderIndexAsc().stream()
            .map(stop -> toStopResponse(studentId, classroomId, stop))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasks(Long studentId, Long classroomId, Long stopId) {
        Stop stop = stopRepository.findById(stopId)
            .orElseThrow(() -> new ResourceNotFoundException("Stop not found"));
        requireUnlocked(studentId, classroomId, stop);

        return taskRepository.findByStop_IdOrderByIdAsc(stopId).stream()
            .map(task -> toTaskResponse(studentId, classroomId, task))
            .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(Long studentId, Long classroomId, Long taskId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
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
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        requireUnlocked(studentId, classroomId, task.getStop());

        String explanation = extractExplanation(task);
        Optional<StudentProgress> existingProgress = studentProgressRepository
            .findByStudent_IdAndTask_IdAndClassroom_Id(studentId, taskId, classroomId);

        if (existingProgress.map(StudentProgress::isCompleted).orElse(false)) {
            return new SubmitAnswerResponse(
                true,
                existingProgress.get().getScore(),
                explanation,
                isStopComplete(studentId, classroomId, task.getStop().getId()),
                null
            );
        }

        if (!checkAnswer(task, req == null ? null : req.answer())) {
            return new SubmitAnswerResponse(false, 0, explanation, false, null);
        }

        StudentProgress progress = existingProgress.orElseGet(StudentProgress::new);
        progress.setStudent(userRepository.getReferenceById(studentId));
        progress.setClassroom(classroomRepository.getReferenceById(classroomId));
        progress.setStop(task.getStop());
        progress.setTask(task);
        progress.setCompleted(true);
        progress.setScore(CORRECT_SCORE);
        progress.setAttempts(progress.getAttempts() + 1);
        progress.setCompletedAt(LocalDateTime.now());
        studentProgressRepository.save(progress);

        boolean stopCompleted = isStopComplete(studentId, classroomId, task.getStop().getId());
        MedalDto medalEarned = stopCompleted
            ? checkAndAwardMedal(studentId, classroomId, task.getStop().getId()).map(this::toMedalDto).orElse(null)
            : null;

        return new SubmitAnswerResponse(true, CORRECT_SCORE, explanation, stopCompleted, medalEarned);
    }

    @Transactional(readOnly = true)
    public ProgressResponse getProgress(Long studentId, Long classroomId) {
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
            .map(previous -> allTasksCompleted(studentId, classroomId, previous.getId()))
            .orElse(false);
    }

    private boolean isStopComplete(Long studentId, Long classroomId, Long stopId) {
        long taskCount = taskRepository.countByStop_Id(stopId);
        return taskCount > 0 && completedTaskCount(studentId, classroomId, stopId) == taskCount;
    }

    private Optional<Medal> checkAndAwardMedal(Long studentId, Long classroomId, Long stopId) {
        Optional<Medal> medal = medalRepository.findByStop_Id(stopId);
        if (medal.isEmpty()) {
            return Optional.empty();
        }

        Long medalId = medal.get().getId();
        if (studentMedalRepository.existsByStudent_IdAndMedal_IdAndClassroom_Id(studentId, medalId, classroomId)) {
            return Optional.empty();
        }

        StudentMedal studentMedal = new StudentMedal();
        studentMedal.setStudent(userRepository.getReferenceById(studentId));
        studentMedal.setClassroom(classroomRepository.getReferenceById(classroomId));
        studentMedal.setMedal(medal.get());
        studentMedalRepository.save(studentMedal);
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
            isStopComplete(studentId, classroomId, stop.getId()),
            taskCount
        );
    }

    private TaskResponse toTaskResponse(Long studentId, Long classroomId, Task task) {
        boolean alreadyCompleted = studentProgressRepository
            .findByStudent_IdAndTask_IdAndClassroom_Id(studentId, task.getId(), classroomId)
            .map(StudentProgress::isCompleted)
            .orElse(false);

        return new TaskResponse(
            task.getId(),
            task.getStop().getId(),
            task.getTaskType().name(),
            task.getContentJson(),
            task.getGuidanceText(),
            alreadyCompleted
        );
    }

    private MedalDto toMedalDto(Medal medal) {
        return new MedalDto(medal.getId(), medal.getName(), medal.getDescription());
    }

    private void requireUnlocked(Long studentId, Long classroomId, Stop stop) {
        if (!isStopUnlocked(studentId, classroomId, stop)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Stop is locked");
        }
    }

    private boolean allTasksCompleted(Long studentId, Long classroomId, Long stopId) {
        long taskCount = taskRepository.countByStop_Id(stopId);
        return completedTaskCount(studentId, classroomId, stopId) == taskCount;
    }

    private long completedTaskCount(Long studentId, Long classroomId, Long stopId) {
        return studentProgressRepository
            .countByStudent_IdAndTask_Stop_IdAndClassroom_IdAndCompletedTrue(studentId, stopId, classroomId);
    }

    private String extractExplanation(Task task) {
        try {
            return objectMapper.readTree(task.getContentJson()).path("explanation").asText("");
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Task content data is invalid");
        }
    }
}
