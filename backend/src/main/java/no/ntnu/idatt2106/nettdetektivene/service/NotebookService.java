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
        return notebookRepository
            .findByStudent_IdOrderByStop_OrderIndexAscCreatedAtAsc(studentId)
            .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<NotebookEntryDto> getEntriesForTeacher(Long teacherId, Long studentId) {
        log.info("[NotebookService] getEntriesForTeacher teacherId={} studentId={}", teacherId, studentId);
        if (!classroomStudentRepository.existsStudentInTeacherClassroom(teacherId, studentId)) {
            log.warn("[NotebookService] teacher {} not authorized for student {}", teacherId, studentId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student not in your classroom");
        }
        return notebookRepository
            .findByStudent_IdOrderByStop_OrderIndexAscCreatedAtAsc(studentId)
            .stream().map(this::toDto).toList();
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

    private NotebookEntryDto toDto(NotebookEntry e) {
        return new NotebookEntryDto(
            e.getId(),
            e.getStop().getId(),
            e.getStop().getName(),
            e.getStop().getOrderIndex(),
            e.getEntryType().name(),
            e.getContent(),
            e.getCreatedAt()
        );
    }
}
