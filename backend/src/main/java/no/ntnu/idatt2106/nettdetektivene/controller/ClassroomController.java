package no.ntnu.idatt2106.nettdetektivene.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.ClassroomResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.CreateClassroomRequest;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.JoinClassroomRequest;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.StudentInClassroomResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.UpdateStudentStatusRequest;
import no.ntnu.idatt2106.nettdetektivene.service.ClassroomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

    @PostMapping("/join")
    @PreAuthorize("hasRole('STUDENT')")
    public StudentInClassroomResponse joinClassroom(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody JoinClassroomRequest request
    ) {
        return classroomService.joinClassroom(currentUserId(userDetails), request);
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

    private Long currentUserId(UserDetails userDetails) {
        return Long.parseLong(userDetails.getUsername());
    }
}
