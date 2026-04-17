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

@Service
@RequiredArgsConstructor
public class NotebookService {

    private static final Logger log = LoggerFactory.getLogger(NotebookService.class);

    private final NotebookRepository notebookRepository;
    private final StopRepository stopRepository;
    private final UserRepository userRepository;
    private final ClassroomStudentRepository classroomStudentRepository;

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

    @Transactional
    public void createAutoTipIfNotExists(Long studentId, Stop stop) {
        if (stop.getAutoTip() == null) return;
        if (notebookRepository.existsByStudent_IdAndStop_IdAndEntryType(
                studentId, stop.getId(), NotebookEntry.EntryType.AUTO_TIP)) return;

        NotebookEntry entry = new NotebookEntry();
        entry.setStudent(userRepository.getReferenceById(studentId));
        entry.setStop(stop);
        entry.setEntryType(NotebookEntry.EntryType.AUTO_TIP);
        entry.setContent(stop.getAutoTip());
        notebookRepository.save(entry);
        log.info("[NotebookService] auto-tip created studentId={} stopId={}", studentId, stop.getId());
    }

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
        if (entry.getEntryType() == NotebookEntry.EntryType.AUTO_TIP) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot edit auto-tips");
        }
        entry.setContent(content.strip());
        log.info("[NotebookService] entry updated studentId={} id={}", studentId, entryId);
        return toDto(entry);
    }

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
        if (entry.getEntryType() == NotebookEntry.EntryType.AUTO_TIP) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete auto-tips");
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
