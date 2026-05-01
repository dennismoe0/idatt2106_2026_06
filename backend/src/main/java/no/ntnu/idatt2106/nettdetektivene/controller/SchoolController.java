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

/**
 * Handles school management for teachers, including creating, joining, and querying schools and their classrooms.
 */
@RestController
@RequestMapping("/api/schools")
@RequiredArgsConstructor
public class SchoolController {
    private static final Logger log = LoggerFactory.getLogger(SchoolController.class);

    private final SchoolService schoolService;

    /**
     * Creates a new school and associates the authenticated teacher as its owner.
     *
     * @param userDetails the authenticated teacher
     * @param request     the school name and details
     * @return 201 Created with the new {@link SchoolResponse}
     */
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

    /**
     * Joins an existing school using the provided join code, associating the authenticated teacher with it.
     *
     * @param userDetails the authenticated teacher
     * @param request     the school join code
     * @return 200 OK with the joined {@link SchoolResponse}
     */
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

    /**
     * Returns the school that the authenticated teacher belongs to.
     *
     * @param userDetails the authenticated teacher
     * @return 200 OK with the teacher's {@link SchoolResponse}
     */
    @GetMapping("/mine")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<SchoolResponse> getMySchool(
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long teacherId = currentUserId(userDetails);
        log.info("[SchoolController] GET /api/schools/mine teacherId={}", teacherId);
        return ResponseEntity.ok(schoolService.getMySchool(teacherId));
    }

    /**
     * Returns a summary of all classrooms in the school that the authenticated teacher belongs to.
     *
     * @param userDetails the authenticated teacher
     * @return 200 OK with a list of {@link SchoolClassroomSummary} for each classroom in the school
     */
    @GetMapping("/mine/classrooms")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<SchoolClassroomSummary>> getSchoolClassrooms(
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long teacherId = currentUserId(userDetails);
        log.info("[SchoolController] GET /api/schools/mine/classrooms teacherId={}", teacherId);
        return ResponseEntity.ok(schoolService.getSchoolClassrooms(teacherId));
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
