package no.ntnu.idatt2106.nettdetektivene.controller;

import lombok.RequiredArgsConstructor;
import no.ntnu.idatt2106.nettdetektivene.dto.game.ClaimXpResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.game.PlayerProfileDto;
import no.ntnu.idatt2106.nettdetektivene.dto.game.ProgressResponse;
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

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameController {

    private static final Logger log = LoggerFactory.getLogger(GameController.class);

    private final GameService gameService;

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

    @GetMapping("/profile")
    @PreAuthorize("hasRole('STUDENT')")
    public PlayerProfileDto getProfile(
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long studentId = currentUserId(userDetails);
        log.info("[GameController] GET /profile studentId={}", studentId);
        return gameService.getProfile(studentId);
    }

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

    private Long currentUserId(UserDetails userDetails) {
        return Long.parseLong(userDetails.getUsername());
    }
}
