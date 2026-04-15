package no.ntnu.idatt2106.nettdetektivene.controller;

import lombok.RequiredArgsConstructor;
import no.ntnu.idatt2106.nettdetektivene.dto.game.ProgressResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.game.StopResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.game.SubmitAnswerRequest;
import no.ntnu.idatt2106.nettdetektivene.dto.game.SubmitAnswerResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.game.TaskResponse;
import no.ntnu.idatt2106.nettdetektivene.service.GameService;
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

    private final GameService gameService;

    @GetMapping("/stops")
    @PreAuthorize("hasRole('STUDENT')")
    public List<StopResponse> getStops(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam Long classroomId
    ) {
        return gameService.getStops(currentUserId(userDetails), classroomId);
    }

    @GetMapping("/stops/{stopId}/tasks")
    @PreAuthorize("hasRole('STUDENT')")
    public List<TaskResponse> getTasks(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long stopId,
        @RequestParam Long classroomId
    ) {
        return gameService.getTasks(currentUserId(userDetails), classroomId, stopId);
    }

    @GetMapping("/tasks/{taskId}")
    @PreAuthorize("hasRole('STUDENT')")
    public TaskResponse getTask(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long taskId,
        @RequestParam Long classroomId
    ) {
        return gameService.getTask(currentUserId(userDetails), classroomId, taskId);
    }

    @PostMapping("/tasks/{taskId}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public SubmitAnswerResponse submitAnswer(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long taskId,
        @RequestParam Long classroomId,
        @RequestBody SubmitAnswerRequest request
    ) {
        return gameService.submitAnswer(currentUserId(userDetails), classroomId, taskId, request);
    }

    @GetMapping("/progress")
    @PreAuthorize("hasRole('STUDENT')")
    public ProgressResponse getProgress(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam Long classroomId
    ) {
        return gameService.getProgress(currentUserId(userDetails), classroomId);
    }

    private Long currentUserId(UserDetails userDetails) {
        return Long.parseLong(userDetails.getUsername());
    }
}
