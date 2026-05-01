package no.ntnu.idatt2106.nettdetektivene.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.ClassroomResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.CreateClassroomRequest;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.JoinClassroomRequest;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.LeaderboardEntryDto;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.SchoolLeaderboardEntryDto;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.StudentInClassroomResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.MusicMutedRequest;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.StudentProgressSummaryDto;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.StudentStatusResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.UpdateDisplayNameRequest;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.UpdateStudentStatusRequest;
import no.ntnu.idatt2106.nettdetektivene.dto.game.StopResponse;
import no.ntnu.idatt2106.nettdetektivene.service.ClassroomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Handles classroom management for teachers and students, including creation, membership,
 * student progress, leaderboards, and classroom settings.
 */
@RestController
@RequestMapping("/api/classrooms")
@RequiredArgsConstructor
public class ClassroomController {
    private static final Logger log = LoggerFactory.getLogger(ClassroomController.class);

    private final ClassroomService classroomService;

    /**
     * Creates a new classroom owned by the authenticated teacher.
     *
     * @param userDetails the authenticated teacher
     * @param request     the classroom name and configuration
     * @return 201 Created with the new {@link ClassroomResponse}
     */
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ClassroomResponse> createClassroom(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody CreateClassroomRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(classroomService.createClassroom(currentUserId(userDetails), request));
    }

    /**
     * Returns all classrooms owned by the authenticated teacher.
     *
     * @param userDetails the authenticated teacher
     * @return a list of {@link ClassroomResponse} for the teacher's classrooms
     */
    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public List<ClassroomResponse> getMyClassrooms(@AuthenticationPrincipal UserDetails userDetails) {
        return classroomService.getMyClassrooms(currentUserId(userDetails));
    }

    /**
     * Returns details for a specific classroom owned by the authenticated teacher.
     *
     * @param userDetails the authenticated teacher
     * @param id          the classroom ID
     * @return the {@link ClassroomResponse} for the requested classroom
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ClassroomResponse getClassroom(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long id
    ) {
        return classroomService.getClassroom(currentUserId(userDetails), id);
    }

    /**
     * Deletes a classroom owned by the authenticated teacher.
     *
     * @param userDetails the authenticated teacher
     * @param id          the classroom ID to delete
     * @return 204 No Content on success
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteClassroom(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long id
    ) {
        log.info("[ClassroomController] DELETE /api/classrooms/{} by teacherId={}", id, currentUserId(userDetails));
        classroomService.deleteClassroom(currentUserId(userDetails), id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Allows the authenticated student to join a classroom using a join code.
     *
     * @param userDetails the authenticated student
     * @param request     the classroom join code
     * @return 201 Created with the student's {@link StudentInClassroomResponse}
     */
    @PostMapping("/join")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentInClassroomResponse> joinClassroom(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody JoinClassroomRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(classroomService.joinClassroom(currentUserId(userDetails), request));
    }

    /**
     * Returns all students enrolled in a specific classroom, accessible by the owning teacher.
     *
     * @param userDetails the authenticated teacher
     * @param id          the classroom ID
     * @return a list of {@link StudentInClassroomResponse} for each student in the classroom
     */
    @GetMapping("/{id}/students")
    @PreAuthorize("hasRole('TEACHER')")
    public List<StudentInClassroomResponse> getStudents(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long id
    ) {
        return classroomService.getStudents(currentUserId(userDetails), id);
    }

    /**
     * Returns a progress summary for each student in the classroom, accessible by the owning teacher.
     *
     * @param userDetails the authenticated teacher
     * @param id          the classroom ID
     * @return a list of {@link StudentProgressSummaryDto} with XP and task completion per student
     */
    @GetMapping("/{id}/student-progress")
    @PreAuthorize("hasRole('TEACHER')")
    public List<StudentProgressSummaryDto> getStudentProgress(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long id
    ) {
        log.info("[ClassroomController] GET /api/classrooms/{}/student-progress teacherId={}", id, currentUserId(userDetails));
        return classroomService.getStudentProgressSummaries(currentUserId(userDetails), id);
    }

    /**
     * Returns all game stops visible to the teacher for a specific classroom.
     *
     * @param userDetails the authenticated teacher
     * @param id          the classroom ID
     * @return a list of {@link StopResponse} for the classroom
     */
    @GetMapping("/{id}/stops")
    @PreAuthorize("hasRole('TEACHER')")
    public List<StopResponse> getStopsForClassroom(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long id
    ) {
        log.info("[ClassroomController] GET /api/classrooms/{}/stops teacherId={}", id, currentUserId(userDetails));
        return classroomService.getStopsForClassroom(currentUserId(userDetails), id);
    }

    /**
     * Updates the approval/kick status of a student in a classroom, accessible by the owning teacher.
     *
     * @param userDetails the authenticated teacher
     * @param id          the classroom ID
     * @param sid         the student ID to update
     * @param request     the new status (e.g. APPROVED, KICKED)
     * @return the updated {@link StudentInClassroomResponse}
     */
    @PutMapping("/{id}/students/{sid}")
    @PreAuthorize("hasRole('TEACHER')")
    public StudentInClassroomResponse updateStudentStatus(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long id,
        @PathVariable Long sid,
        @Valid @RequestBody UpdateStudentStatusRequest request
    ) {
        return classroomService.updateStudentStatus(currentUserId(userDetails), id, sid, request.status());
    }

    /**
     * Returns the XP-ranked leaderboard for a specific classroom, accessible by any authenticated user.
     *
     * @param id the classroom ID
     * @return a list of {@link LeaderboardEntryDto} ordered by XP descending
     */
    @GetMapping("/{id}/leaderboard")
    @PreAuthorize("isAuthenticated()")
    public List<LeaderboardEntryDto> getLeaderboard(@PathVariable Long id) {
        log.info("[ClassroomController] GET /api/classrooms/{}/leaderboard", id);
        return classroomService.getLeaderboard(id);
    }

    /**
     * Returns the leaderboard ranking classrooms within the same school, scoped to the current classroom's school.
     *
     * @param userDetails the authenticated student or teacher
     * @param id          the classroom ID used to determine which school to scope the leaderboard to
     * @return a list of {@link SchoolLeaderboardEntryDto} ordered by average XP descending
     */
    @GetMapping("/{id}/school-leaderboard")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public List<SchoolLeaderboardEntryDto> getSchoolLeaderboard(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long id
    ) {
        log.info("[ClassroomController] GET /api/classrooms/{}/school-leaderboard", id);
        return classroomService.getSchoolLeaderboard(currentUserId(userDetails), id);
    }

    /**
     * Returns the global leaderboard ranking classrooms across all schools.
     *
     * @param userDetails the authenticated student or teacher
     * @param id          the classroom ID used as context for the requesting user
     * @return a list of {@link SchoolLeaderboardEntryDto} ordered by average XP descending
     */
    @GetMapping("/{id}/global-leaderboard")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public List<SchoolLeaderboardEntryDto> getGlobalLeaderboard(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long id
    ) {
        log.info("[ClassroomController] GET /api/classrooms/{}/global-leaderboard", id);
        return classroomService.getGlobalLeaderboard(currentUserId(userDetails), id);
    }

    /**
     * Updates the display name of the authenticated student within a specific classroom.
     *
     * @param userDetails the authenticated student
     * @param id          the classroom ID
     * @param request     the new display name
     * @return the updated {@link StudentInClassroomResponse}
     */
    @PutMapping("/{id}/my-displayname")
    @PreAuthorize("hasRole('STUDENT')")
    public StudentInClassroomResponse updateMyDisplayName(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody UpdateDisplayNameRequest request
    ) {
        Long studentId = currentUserId(userDetails);
        log.info("[ClassroomController] PUT /api/classrooms/{}/my-displayname studentId={}", id, studentId);
        return classroomService.updateMyDisplayName(studentId, id, request.displayName());
    }

    /**
     * Returns the classroom that the authenticated student is currently enrolled in, if any.
     *
     * @param userDetails the authenticated student
     * @return 200 OK with the student's {@link StudentInClassroomResponse}, or 404 if not enrolled in any classroom
     */
    @GetMapping("/mine")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentInClassroomResponse> getMyClassroom(
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long studentId = currentUserId(userDetails);
        log.info("[ClassroomController] GET /api/classrooms/mine studentId={}", studentId);
        return classroomService.getMyClassroom(studentId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Returns the authenticated student's enrollment status within a specific classroom.
     *
     * @param userDetails the authenticated student
     * @param id          the classroom ID
     * @return 200 OK with a {@link StudentStatusResponse} containing the student's current status
     */
    @GetMapping("/{id}/my-status")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentStatusResponse> getMyStatus(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long id
    ) {
        Long studentId = currentUserId(userDetails);
        log.info("[ClassroomController] GET /api/classrooms/{}/my-status studentId={}", id, studentId);
        return ResponseEntity.ok(classroomService.getMyStatus(studentId, id));
    }

    /**
     * Sets the music-muted flag for a classroom, allowing teachers to mute background music for all students.
     *
     * @param userDetails the authenticated teacher
     * @param id          the classroom ID
     * @param request     the desired muted state
     * @return 200 OK with no body on success
     */
    @PutMapping("/{id}/music-muted")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> setMusicMuted(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long id,
        @RequestBody MusicMutedRequest request
    ) {
        Long teacherId = currentUserId(userDetails);
        log.info("[ClassroomController] PUT /api/classrooms/{}/music-muted teacherId={} muted={}", id, teacherId, request.musicMuted());
        classroomService.setMusicMuted(teacherId, id, request.musicMuted());
        return ResponseEntity.ok().build();
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
