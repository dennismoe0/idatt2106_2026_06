package no.ntnu.idatt2106.nettdetektivene.controller;

import lombok.RequiredArgsConstructor;
import no.ntnu.idatt2106.nettdetektivene.dto.game.ClaimXpResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.game.PlayerProfileDto;
import no.ntnu.idatt2106.nettdetektivene.dto.game.ProgressResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.game.StopMetaResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.game.StopResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.game.SubmitAnswerRequest;
import no.ntnu.idatt2106.nettdetektivene.dto.game.SubmitAnswerResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.game.TaskResponse;
import no.ntnu.idatt2106.nettdetektivene.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Handles game-related endpoints for students and teachers, including stop and task retrieval,
 * answer submission, progress tracking, and weekly XP claims.
 */
@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameController {

    private static final Logger log = LoggerFactory.getLogger(GameController.class);

    private final GameService gameService;

    /**
     * Returns metadata for all stops in the game, intended for teacher use (e.g. managing classroom progress).
     *
     * @return a list of {@link StopMetaResponse} for every stop
     */
    @GetMapping("/stops/meta")
    @PreAuthorize("hasRole('TEACHER')")
    public List<StopMetaResponse> getStopsMeta() {
        log.info("[GameController] GET /stops/meta");
        return gameService.getStopsMeta();
    }

    /**
     * Returns all stops for the given classroom with their locked/unlocked status for the authenticated student.
     *
     * @param userDetails the authenticated student
     * @param classroomId the classroom to load stop status for
     * @return a list of {@link StopResponse} with lock status per stop
     */
    @GetMapping("/stops")
    @PreAuthorize("hasRole('STUDENT')")
    public List<StopResponse> getStops(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam Long classroomId
    ) {
        Long studentId = currentUserId(userDetails);
        log.info("[GameController] GET /stops studentId={} classroomId={}", studentId, classroomId);
        return gameService.getStops(studentId, classroomId);
    }

    /**
     * Returns all tasks for a given stop in the context of the authenticated student's classroom progress.
     *
     * @param userDetails the authenticated student
     * @param stopId      the stop whose tasks to retrieve
     * @param classroomId the classroom context
     * @return a list of {@link TaskResponse} for the stop
     */
    @GetMapping("/stops/{stopId}/tasks")
    @PreAuthorize("hasRole('STUDENT')")
    public List<TaskResponse> getTasks(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long stopId,
        @RequestParam Long classroomId
    ) {
        Long studentId = currentUserId(userDetails);
        log.info(
            "[GameController] GET /stops/{stopId}/tasks studentId={} classroomId={} stopId={}",
            studentId,
            classroomId,
            stopId
        );
        return gameService.getTasks(studentId, classroomId, stopId);
    }

    /**
     * Returns a single task by ID in the context of the authenticated student's classroom.
     *
     * @param userDetails the authenticated student
     * @param taskId      the task to retrieve
     * @param classroomId the classroom context
     * @return the {@link TaskResponse} for the requested task
     */
    @GetMapping("/tasks/{taskId}")
    @PreAuthorize("hasRole('STUDENT')")
    public TaskResponse getTask(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long taskId,
        @RequestParam Long classroomId
    ) {
        Long studentId = currentUserId(userDetails);
        log.info(
            "[GameController] GET /tasks/{taskId} studentId={} classroomId={} taskId={}",
            studentId,
            classroomId,
            taskId
        );
        return gameService.getTask(studentId, classroomId, taskId);
    }

    /**
     * Submits a student's answer for a task and returns the result including correctness and XP awarded.
     *
     * @param userDetails the authenticated student
     * @param taskId      the task being answered
     * @param classroomId the classroom context
     * @param request     the student's answer payload
     * @return a {@link SubmitAnswerResponse} with correctness feedback and XP
     */
    @PostMapping("/tasks/{taskId}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public SubmitAnswerResponse submitAnswer(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long taskId,
        @RequestParam Long classroomId,
        @RequestBody SubmitAnswerRequest request
    ) {
        Long studentId = currentUserId(userDetails);
        log.info(
            "[GameController] POST /tasks/{taskId}/submit studentId={} classroomId={} taskId={}",
            studentId,
            classroomId,
            taskId
        );
        return gameService.submitAnswer(studentId, classroomId, taskId, request);
    }

    /**
     * Returns the authenticated student's overall game progress for the given classroom.
     *
     * @param userDetails the authenticated student
     * @param classroomId the classroom to load progress for
     * @return a {@link ProgressResponse} with XP, completed stops, and task counts
     */
    @GetMapping("/progress")
    @PreAuthorize("hasRole('STUDENT')")
    public ProgressResponse getProgress(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam Long classroomId
    ) {
        Long studentId = currentUserId(userDetails);
        log.info("[GameController] GET /progress studentId={} classroomId={}", studentId, classroomId);
        return gameService.getProgress(studentId, classroomId);
    }

    /**
     * Returns the authenticated student's player profile including display name, XP, level, and medals.
     *
     * @param userDetails the authenticated student
     * @return a {@link PlayerProfileDto} for the student
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('STUDENT')")
    public PlayerProfileDto getProfile(
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long studentId = currentUserId(userDetails);
        log.info("[GameController] GET /profile studentId={}", studentId);
        return gameService.getProfile(studentId);
    }

    /**
     * Claims the weekly bonus XP for completing a stop, if not already claimed this week.
     *
     * @param userDetails the authenticated student
     * @param stopId      the stop for which to claim weekly XP
     * @return a {@link ClaimXpResponse} indicating whether XP was awarded and the amount
     */
    @PostMapping("/stops/{stopId}/claim-xp")
    @PreAuthorize("hasRole('STUDENT')")
    public ClaimXpResponse claimWeeklyXp(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long stopId
    ) {
        Long studentId = currentUserId(userDetails);
        log.info("[GameController] POST /stops/{}/claim-xp studentId={} stopId={}", studentId, stopId);
        return gameService.claimWeeklyXp(studentId, stopId);
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
