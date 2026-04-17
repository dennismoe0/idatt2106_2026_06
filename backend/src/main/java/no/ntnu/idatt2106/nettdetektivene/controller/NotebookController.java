package no.ntnu.idatt2106.nettdetektivene.controller;

import lombok.RequiredArgsConstructor;
import no.ntnu.idatt2106.nettdetektivene.dto.notebook.CreateReflectionRequest;
import no.ntnu.idatt2106.nettdetektivene.dto.notebook.GeneralNoteRequest;
import no.ntnu.idatt2106.nettdetektivene.dto.notebook.NotebookEntryDto;
import no.ntnu.idatt2106.nettdetektivene.service.NotebookService;
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
@RequestMapping("/api/notebook")
@RequiredArgsConstructor
public class NotebookController {

    private static final Logger log = LoggerFactory.getLogger(NotebookController.class);

    private final NotebookService notebookService;

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public List<NotebookEntryDto> getMyEntries(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long studentId = Long.parseLong(userDetails.getUsername());
        log.info("[NotebookController] GET /notebook studentId={}", studentId);
        return notebookService.getEntries(studentId);
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<NotebookEntryDto> createReflection(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreateReflectionRequest request) {
        Long studentId = Long.parseLong(userDetails.getUsername());
        log.info("[NotebookController] POST /notebook studentId={} stopId={}", studentId, request.stopId());
        NotebookEntryDto created = notebookService.createReflection(studentId, request.stopId(), request.content());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/general")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<NotebookEntryDto> createGeneralNote(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody GeneralNoteRequest request) {
        Long studentId = Long.parseLong(userDetails.getUsername());
        log.info("[NotebookController] POST /notebook/general studentId={}", studentId);
        NotebookEntryDto created = notebookService.createGeneralNote(studentId, request.content());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public NotebookEntryDto updateEntry(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody GeneralNoteRequest request) {
        Long studentId = Long.parseLong(userDetails.getUsername());
        log.info("[NotebookController] PUT /notebook/{} studentId={}", id, studentId);
        return notebookService.updateEntry(studentId, id, request.content());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> deleteEntry(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long studentId = Long.parseLong(userDetails.getUsername());
        log.info("[NotebookController] DELETE /notebook/{} studentId={}", id, studentId);
        notebookService.deleteEntry(studentId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('TEACHER')")
    public List<NotebookEntryDto> getStudentEntries(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long studentId) {
        Long teacherId = Long.parseLong(userDetails.getUsername());
        log.info("[NotebookController] GET /notebook/student/{} teacherId={}", studentId, teacherId);
        return notebookService.getEntriesForTeacher(teacherId, studentId);
    }
}
