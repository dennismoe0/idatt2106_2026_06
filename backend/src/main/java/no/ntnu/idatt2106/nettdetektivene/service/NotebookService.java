package no.ntnu.idatt2106.nettdetektivene.service;

import lombok.RequiredArgsConstructor;
import no.ntnu.idatt2106.nettdetektivene.dto.notebook.NotebookEntryDto;
import no.ntnu.idatt2106.nettdetektivene.entity.NotebookEntry;
import no.ntnu.idatt2106.nettdetektivene.entity.Stop;
import no.ntnu.idatt2106.nettdetektivene.exception.ResourceNotFoundException;
import no.ntnu.idatt2106.nettdetektivene.repository.ClassroomStudentRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.NotebookRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.StopRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages student notebook entries including auto-generated tips and clues, student reflections, and general notes.
 */
@Service
@RequiredArgsConstructor
public class NotebookService {

    private static final Logger log = LoggerFactory.getLogger(NotebookService.class);

    private final NotebookRepository notebookRepository;
    private final StopRepository stopRepository;
    private final UserRepository userRepository;
    private final ClassroomStudentRepository classroomStudentRepository;

    /**
     * Returns all notebook entries for a student, ordered with general notes first, then stop entries by stop order.
     *
     * @param studentId the student's user ID
     * @return ordered list of {@link NotebookEntryDto}
     */
    @Transactional(readOnly = true)
    public List<NotebookEntryDto> getEntries(Long studentId) {
        log.info("[NotebookService] getEntries studentId={}", studentId);
        List<NotebookEntryDto> result = new ArrayList<>();
        notebookRepository.findByStudent_IdAndStop_IsNullOrderByCreatedAtAsc(studentId)
            .stream().map(this::toDto).forEach(result::add);
        notebookRepository.findByStudent_IdAndStop_IsNotNullOrderByStop_OrderIndexAscCreatedAtAsc(studentId)
            .stream().map(this::toDto).forEach(result::add);
        return result;
    }

    /**
     * Returns all notebook entries for a student, accessible only by a teacher who has that student in one of their classrooms.
     *
     * @param teacherId the teacher's user ID
     * @param studentId the student's user ID
     * @return ordered list of {@link NotebookEntryDto}
     * @throws org.springframework.web.server.ResponseStatusException if the teacher is not authorized for this student
     */
    @Transactional(readOnly = true)
    public List<NotebookEntryDto> getEntriesForTeacher(Long teacherId, Long studentId) {
        log.info("[NotebookService] getEntriesForTeacher teacherId={} studentId={}", teacherId, studentId);
        if (!classroomStudentRepository.existsStudentInTeacherClassroom(teacherId, studentId)) {
            log.warn("[NotebookService] teacher {} not authorized for student {}", teacherId, studentId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student not in your classroom");
        }
        List<NotebookEntryDto> result = new ArrayList<>();
        notebookRepository.findByStudent_IdAndStop_IsNullOrderByCreatedAtAsc(studentId)
            .stream().map(this::toDto).forEach(result::add);
        notebookRepository.findByStudent_IdAndStop_IsNotNullOrderByStop_OrderIndexAscCreatedAtAsc(studentId)
            .stream().map(this::toDto).forEach(result::add);
        return result;
    }

    /**
     * Creates an AUTO_TIP notebook entry for the given stop if one does not already exist.
     *
     * @param studentId the student's user ID
     * @param stop      the stop whose auto-tip should be recorded
     */
    @Transactional
    public void createAutoTipIfNotExists(Long studentId, Stop stop) {
        String content = stop.getAutoTip();
        if (content == null || content.isBlank()) return;
        if (notebookRepository.existsByStudent_IdAndStop_IdAndEntryType(
                studentId, stop.getId(), NotebookEntry.EntryType.AUTO_TIP)) return;

        NotebookEntry entry = new NotebookEntry();
        entry.setStudent(userRepository.getReferenceById(studentId));
        entry.setStop(stop);
        entry.setEntryType(NotebookEntry.EntryType.AUTO_TIP);
        entry.setContent(content);
        notebookRepository.save(entry);
        log.info("[NotebookService] auto-tip created studentId={} stopId={}", studentId, stop.getId());
    }

    /**
     * Creates an AUTO_CLUE notebook entry for the given stop (and ensures the auto-tip is also present).
     *
     * @param studentId the student's user ID
     * @param stop      the stop whose clue text should be recorded
     */
    @Transactional
    public void createAutoClueIfNotExists(Long studentId, Stop stop) {
        // Always ensure the auto-tip is captured as well, so the tip and the
        // clue live side-by-side in the journal instead of overwriting each other.
        createAutoTipIfNotExists(studentId, stop);

        String content = stop.getClueText();
        if (content == null || content.isBlank()) return;
        if (notebookRepository.existsByStudent_IdAndStop_IdAndEntryType(
                studentId, stop.getId(), NotebookEntry.EntryType.AUTO_CLUE)) return;

        NotebookEntry entry = new NotebookEntry();
        entry.setStudent(userRepository.getReferenceById(studentId));
        entry.setStop(stop);
        entry.setEntryType(NotebookEntry.EntryType.AUTO_CLUE);
        entry.setContent(content);
        notebookRepository.save(entry);
        log.info("[NotebookService] auto-clue created studentId={} stopId={}", studentId, stop.getId());
    }

    /**
     * Creates a student reflection entry for a completed stop.
     *
     * @param studentId the student's user ID
     * @param stopId    the stop being reflected on
     * @param content   the reflection text
     * @return the created {@link NotebookEntryDto}
     * @throws org.springframework.web.server.ResponseStatusException if the stop is not yet completed
     */
    @Transactional
    public NotebookEntryDto createReflection(Long studentId, Long stopId, String content) {
        Stop stop = stopRepository.findById(stopId).orElseThrow(() -> {
            log.warn("[NotebookService] stop not found stopId={}", stopId);
            return new ResourceNotFoundException("Stop not found");
        });
        if (!notebookRepository.existsByStudent_IdAndStop_IdAndEntryType(
                studentId, stopId, NotebookEntry.EntryType.AUTO_TIP)) {
            log.warn("[NotebookService] reflection rejected — stop not completed studentId={} stopId={}", studentId, stopId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Stop not yet completed");
        }
        NotebookEntry entry = new NotebookEntry();
        entry.setStudent(userRepository.getReferenceById(studentId));
        entry.setStop(stop);
        entry.setEntryType(NotebookEntry.EntryType.REFLECTION);
        entry.setContent(content.strip());
        NotebookEntry saved = notebookRepository.save(entry);
        log.info("[NotebookService] reflection created studentId={} stopId={}", studentId, stopId);
        return toDto(saved);
    }

    /**
     * Creates a free-form general note not tied to any specific stop.
     *
     * @param studentId the student's user ID
     * @param content   the note text
     * @return the created {@link NotebookEntryDto}
     */
    @Transactional
    public NotebookEntryDto createGeneralNote(Long studentId, String content) {
        NotebookEntry entry = new NotebookEntry();
        entry.setStudent(userRepository.getReferenceById(studentId));
        entry.setEntryType(NotebookEntry.EntryType.GENERAL_NOTE);
        entry.setContent(content.strip());
        NotebookEntry saved = notebookRepository.save(entry);
        log.info("[NotebookService] general note created studentId={} id={}", studentId, saved.getId());
        return toDto(saved);
    }

    /**
     * Updates the text content of an existing editable notebook entry owned by the student.
     *
     * @param studentId the student's user ID
     * @param entryId   the entry ID to update
     * @param content   the new text content
     * @return the updated {@link NotebookEntryDto}
     * @throws org.springframework.web.server.ResponseStatusException if the entry is not owned by the student or is auto-generated
     */
    @Transactional
    public NotebookEntryDto updateEntry(Long studentId, Long entryId, String content) {
        NotebookEntry entry = notebookRepository.findById(entryId).orElseThrow(() -> {
            log.warn("[NotebookService] entry not found id={}", entryId);
            return new ResourceNotFoundException("Entry not found");
        });
        if (!entry.getStudent().getId().equals(studentId)) {
            log.warn("[NotebookService] update rejected — not owner studentId={} entryId={}", studentId, entryId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your entry");
        }
        if (entry.getEntryType() == NotebookEntry.EntryType.AUTO_TIP
                || entry.getEntryType() == NotebookEntry.EntryType.AUTO_CLUE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot edit auto-generated entries");
        }
        entry.setContent(content.strip());
        log.info("[NotebookService] entry updated studentId={} id={}", studentId, entryId);
        return toDto(entry);
    }

    /**
     * Deletes an editable notebook entry owned by the student.
     *
     * @param studentId the student's user ID
     * @param entryId   the entry ID to delete
     * @throws org.springframework.web.server.ResponseStatusException if the entry is not owned by the student or is auto-generated
     */
    @Transactional
    public void deleteEntry(Long studentId, Long entryId) {
        NotebookEntry entry = notebookRepository.findById(entryId).orElseThrow(() -> {
            log.warn("[NotebookService] entry not found id={}", entryId);
            return new ResourceNotFoundException("Entry not found");
        });
        if (!entry.getStudent().getId().equals(studentId)) {
            log.warn("[NotebookService] delete rejected — not owner studentId={} entryId={}", studentId, entryId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your entry");
        }
        if (entry.getEntryType() == NotebookEntry.EntryType.AUTO_TIP
                || entry.getEntryType() == NotebookEntry.EntryType.AUTO_CLUE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete auto-generated entries");
        }
        notebookRepository.delete(entry);
        log.info("[NotebookService] entry deleted studentId={} id={}", studentId, entryId);
    }

    private NotebookEntryDto toDto(NotebookEntry e) {
        Stop stop = e.getStop();
        return new NotebookEntryDto(
            e.getId(),
            stop != null ? stop.getId() : null,
            stop != null ? stop.getName() : null,
            stop != null ? stop.getOrderIndex() : null,
            e.getEntryType().name(),
            e.getContent(),
            e.getCreatedAt()
        );
    }
}
