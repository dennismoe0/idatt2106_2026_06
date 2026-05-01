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

/**
 * Handles notebook entries for students (reflections and general notes) and allows teachers to read student entries.
 */
@RestController
@RequestMapping("/api/notebook")
@RequiredArgsConstructor
public class NotebookController {

    private static final Logger log = LoggerFactory.getLogger(NotebookController.class);

    private final NotebookService notebookService;

    /**
     * Returns all notebook entries for the authenticated student.
     *
     * @param userDetails the authenticated student
     * @return a list of {@link NotebookEntryDto} belonging to the student
     */
    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public List<NotebookEntryDto> getMyEntries(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long studentId = Long.parseLong(userDetails.getUsername());
        log.info("[NotebookController] GET /notebook studentId={}", studentId);
        return notebookService.getEntries(studentId);
    }

    /**
     * Creates a reflection entry tied to a specific game stop for the authenticated student.
     *
     * @param userDetails the authenticated student
     * @param request     the reflection content and stop ID
     * @return 201 Created with the new {@link NotebookEntryDto}
     */
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

    /**
     * Creates a free-form general note (not tied to a stop) for the authenticated student.
     *
     * @param userDetails the authenticated student
     * @param request     the note content
     * @return 201 Created with the new {@link NotebookEntryDto}
     */
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

    /**
     * Updates the content of an existing notebook entry owned by the authenticated student.
     *
     * @param userDetails the authenticated student
     * @param id          the ID of the entry to update
     * @param request     the new content for the entry
     * @return the updated {@link NotebookEntryDto}
     */
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

    /**
     * Deletes a notebook entry owned by the authenticated student.
     *
     * @param userDetails the authenticated student
     * @param id          the ID of the entry to delete
     * @return 204 No Content on success
     */
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

    /**
     * Returns all notebook entries for a specific student, accessible only to teachers.
     *
     * @param userDetails the authenticated teacher
     * @param studentId   the ID of the student whose entries to retrieve
     * @return a list of {@link NotebookEntryDto} for the given student
     */
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
