package no.ntnu.idatt2106.nettdetektivene.controller;

import jakarta.validation.Valid;
import no.ntnu.idatt2106.nettdetektivene.dto.MysteryCompleteDto;
import no.ntnu.idatt2106.nettdetektivene.dto.MysteryCompleteResultDto;
import no.ntnu.idatt2106.nettdetektivene.dto.WeeklyMysteryEditDto;
import no.ntnu.idatt2106.nettdetektivene.dto.WeeklyMysteryResponseDto;
import no.ntnu.idatt2106.nettdetektivene.dto.WeeklyMysterySubmissionDto;
import no.ntnu.idatt2106.nettdetektivene.entity.User;
import no.ntnu.idatt2106.nettdetektivene.entity.WeeklyMystery;
import no.ntnu.idatt2106.nettdetektivene.repository.UserRepository;
import no.ntnu.idatt2106.nettdetektivene.service.WeeklyMysteryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/weekly-mysteries")
public class WeeklyMysteryController {

    private static final Logger log = LoggerFactory.getLogger(WeeklyMysteryController.class);

    private final WeeklyMysteryService weeklyMysteryService;
    private final UserRepository userRepository;

    public WeeklyMysteryController(WeeklyMysteryService weeklyMysteryService,
                                   UserRepository userRepository) {
        this.weeklyMysteryService = weeklyMysteryService;
        this.userRepository = userRepository;
    }

    // -------------------------------------------------------------------------
    // Student: submit a mystery
    // -------------------------------------------------------------------------

    @PostMapping("/submissions")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<WeeklyMysteryResponseDto> submitMystery(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody WeeklyMysterySubmissionDto dto
    ) {
        Long studentId = currentUserId(userDetails);
        log.info("[WeeklyMysteryController] POST /submissions studentId={} classroomId={}", studentId, dto.classroomId());
        User student = userRepository.getReferenceById(studentId);
        WeeklyMystery saved = weeklyMysteryService.submitMystery(student, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponseDto(saved));
    }

    // -------------------------------------------------------------------------
    // Student: get active mystery for classroom (204 if none)
    // -------------------------------------------------------------------------

    @GetMapping("/active")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<WeeklyMysteryResponseDto> getActiveMystery(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long classroomId
    ) {
        Long studentId = currentUserId(userDetails);
        log.info("[WeeklyMysteryController] GET /active studentId={} classroomId={}", studentId, classroomId);
        WeeklyMystery mystery = weeklyMysteryService.getActiveMystery(classroomId);
        if (mystery == null) {
            log.info("[WeeklyMysteryController] no active mystery for classroomId={}", classroomId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(toResponseDto(mystery));
    }

    // -------------------------------------------------------------------------
    // Student: submit answer for active mystery
    // -------------------------------------------------------------------------

    @PostMapping("/active/complete")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<MysteryCompleteResultDto> completeMystery(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MysteryCompleteDto dto
    ) {
        Long studentId = currentUserId(userDetails);
        log.info("[WeeklyMysteryController] POST /active/complete studentId={} classroomId={}", studentId, dto.classroomId());
        User student = userRepository.getReferenceById(studentId);
        MysteryCompleteResultDto result = weeklyMysteryService.completeMystery(student, dto);
        return ResponseEntity.ok(result);
    }

    // -------------------------------------------------------------------------
    // Teacher: list all submissions for classroom
    // -------------------------------------------------------------------------

    @GetMapping("/submissions")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<WeeklyMysteryResponseDto>> getSubmissions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long classroomId
    ) {
        Long teacherId = currentUserId(userDetails);
        log.info("[WeeklyMysteryController] GET /submissions teacherId={} classroomId={}", teacherId, classroomId);
        List<WeeklyMystery> submissions = weeklyMysteryService.getSubmissions(classroomId);
        List<WeeklyMysteryResponseDto> response = submissions.stream()
                .map(this::toResponseDto)
                .toList();
        return ResponseEntity.ok(response);
    }

    // -------------------------------------------------------------------------
    // Teacher: edit/approve a mystery
    // -------------------------------------------------------------------------

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<WeeklyMysteryResponseDto> editMystery(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody WeeklyMysteryEditDto dto
    ) {
        Long teacherId = currentUserId(userDetails);
        log.info("[WeeklyMysteryController] PUT /{} teacherId={}", id, teacherId);
        WeeklyMystery updated = weeklyMysteryService.editMystery(id, dto, teacherId);
        return ResponseEntity.ok(toResponseDto(updated));
    }

    // -------------------------------------------------------------------------
    // Teacher: activate (feature) a mystery for classroom
    // -------------------------------------------------------------------------

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<WeeklyMysteryResponseDto> activateMystery(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestParam Long classroomId
    ) {
        Long teacherId = currentUserId(userDetails);
        log.info("[WeeklyMysteryController] PUT /{}/activate teacherId={} classroomId={}", id, teacherId, classroomId);
        WeeklyMystery activated = weeklyMysteryService.activateMystery(id, classroomId, teacherId);
        return ResponseEntity.ok(toResponseDto(activated));
    }

    // -------------------------------------------------------------------------
    // Teacher: reject a mystery
    // -------------------------------------------------------------------------

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<WeeklyMysteryResponseDto> rejectMystery(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        Long teacherId = currentUserId(userDetails);
        log.info("[WeeklyMysteryController] PUT /{}/reject teacherId={}", id, teacherId);
        WeeklyMystery rejected = weeklyMysteryService.rejectMystery(id, teacherId);
        return ResponseEntity.ok(toResponseDto(rejected));
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Long currentUserId(UserDetails userDetails) {
        return Long.parseLong(userDetails.getUsername());
    }

    private WeeklyMysteryResponseDto toResponseDto(WeeklyMystery m) {
        String displayName = "Anonym elev";
        return new WeeklyMysteryResponseDto(
                m.getId(), m.getTitle(), m.getDescription(), m.getImageUrl(),
                m.getStatus().name(), m.isFeatured(),
                m.getMysteryType(), m.getQuestionText(), m.getTeacherComment(),
                m.getRewardStars(), m.getRewardXp(), m.getCreatedAt(), displayName
        );
    }
}
