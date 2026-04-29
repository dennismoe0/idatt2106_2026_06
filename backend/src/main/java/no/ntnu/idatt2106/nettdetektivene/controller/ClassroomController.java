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
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.StudentStatusResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.UpdateDisplayNameRequest;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.UpdateStudentStatusRequest;
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

@RestController
@RequestMapping("/api/classrooms")
@RequiredArgsConstructor
public class ClassroomController {
    private static final Logger log = LoggerFactory.getLogger(ClassroomController.class);

    private final ClassroomService classroomService;

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ClassroomResponse> createClassroom(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody CreateClassroomRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(classroomService.createClassroom(currentUserId(userDetails), request));
    }

    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public List<ClassroomResponse> getMyClassrooms(@AuthenticationPrincipal UserDetails userDetails) {
        return classroomService.getMyClassrooms(currentUserId(userDetails));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ClassroomResponse getClassroom(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long id
    ) {
        return classroomService.getClassroom(currentUserId(userDetails), id);
    }

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

    @PostMapping("/join")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentInClassroomResponse> joinClassroom(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody JoinClassroomRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(classroomService.joinClassroom(currentUserId(userDetails), request));
    }

    @GetMapping("/{id}/students")
    @PreAuthorize("hasRole('TEACHER')")
    public List<StudentInClassroomResponse> getStudents(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long id
    ) {
        return classroomService.getStudents(currentUserId(userDetails), id);
    }

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

    @GetMapping("/{id}/leaderboard")
    @PreAuthorize("isAuthenticated()")
    public List<LeaderboardEntryDto> getLeaderboard(@PathVariable Long id) {
        log.info("[ClassroomController] GET /api/classrooms/{}/leaderboard", id);
        return classroomService.getLeaderboard(id);
    }

    @GetMapping("/{id}/school-leaderboard")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public List<SchoolLeaderboardEntryDto> getSchoolLeaderboard(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long id
    ) {
        log.info("[ClassroomController] GET /api/classrooms/{}/school-leaderboard", id);
        return classroomService.getSchoolLeaderboard(currentUserId(userDetails), id);
    }

    @GetMapping("/{id}/global-leaderboard")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public List<SchoolLeaderboardEntryDto> getGlobalLeaderboard(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long id
    ) {
        log.info("[ClassroomController] GET /api/classrooms/{}/global-leaderboard", id);
        return classroomService.getGlobalLeaderboard(currentUserId(userDetails), id);
    }

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

    private Long currentUserId(UserDetails userDetails) {
        return Long.parseLong(userDetails.getUsername());
    }
}
