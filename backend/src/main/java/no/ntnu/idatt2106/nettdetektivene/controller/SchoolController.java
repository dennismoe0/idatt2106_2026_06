package no.ntnu.idatt2106.nettdetektivene.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import no.ntnu.idatt2106.nettdetektivene.dto.school.CreateSchoolRequest;
import no.ntnu.idatt2106.nettdetektivene.dto.school.JoinSchoolRequest;
import no.ntnu.idatt2106.nettdetektivene.dto.school.SchoolClassroomSummary;
import no.ntnu.idatt2106.nettdetektivene.dto.school.SchoolResponse;
import no.ntnu.idatt2106.nettdetektivene.service.SchoolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schools")
@RequiredArgsConstructor
public class SchoolController {
    private static final Logger log = LoggerFactory.getLogger(SchoolController.class);

    private final SchoolService schoolService;

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<SchoolResponse> createSchool(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody CreateSchoolRequest request
    ) {
        Long teacherId = currentUserId(userDetails);
        log.info("[SchoolController] POST /api/schools teacherId={}", teacherId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(schoolService.createSchool(teacherId, request));
    }

    @PostMapping("/join")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<SchoolResponse> joinSchool(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody JoinSchoolRequest request
    ) {
        Long teacherId = currentUserId(userDetails);
        log.info("[SchoolController] POST /api/schools/join teacherId={}", teacherId);
        return ResponseEntity.ok(schoolService.joinSchool(teacherId, request));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<SchoolResponse> getMySchool(
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long teacherId = currentUserId(userDetails);
        log.info("[SchoolController] GET /api/schools/mine teacherId={}", teacherId);
        return ResponseEntity.ok(schoolService.getMySchool(teacherId));
    }

    @GetMapping("/mine/classrooms")
    @PreAuthorize("hasRole('TEACHER')")
    public List<SchoolClassroomSummary> getSchoolClassrooms(
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long teacherId = currentUserId(userDetails);
        log.info("[SchoolController] GET /api/schools/mine/classrooms teacherId={}", teacherId);
        return schoolService.getSchoolClassrooms(teacherId);
    }

    private Long currentUserId(UserDetails userDetails) {
        return Long.parseLong(userDetails.getUsername());
    }
}
