package no.ntnu.idatt2106.nettdetektivene.service;

import no.ntnu.idatt2106.nettdetektivene.dto.notebook.NotebookEntryDto;
import no.ntnu.idatt2106.nettdetektivene.entity.NotebookEntry;
import no.ntnu.idatt2106.nettdetektivene.entity.Stop;
import no.ntnu.idatt2106.nettdetektivene.entity.User;
import no.ntnu.idatt2106.nettdetektivene.exception.ResourceNotFoundException;
import no.ntnu.idatt2106.nettdetektivene.repository.ClassroomStudentRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.NotebookRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.StopRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotebookServiceTest {

    @Mock NotebookRepository notebookRepository;
    @Mock StopRepository stopRepository;
    @Mock UserRepository userRepository;
    @Mock ClassroomStudentRepository classroomStudentRepository;

    NotebookService notebookService;

    @BeforeEach
    void setUp() {
        notebookService = new NotebookService(
            notebookRepository, stopRepository, userRepository, classroomStudentRepository);
    }

    private Stop makeStop(Long id, int order) {
        Stop s = new Stop();
        s.setId(id);
        s.setName("Stop" + id);
        s.setOrderIndex(order);
        return s;
    }

    private NotebookEntry makeEntry(Long id, Stop stop, NotebookEntry.EntryType type, String content) {
        NotebookEntry e = new NotebookEntry();
        e.setId(id);
        e.setStop(stop);
        e.setEntryType(type);
        e.setContent(content);
        return e;
    }

    // ── getEntries ──────────────────────────────────────────────

    @Test
    void getEntries_returnsEmptyListWhenNoEntries() {
        when(notebookRepository.findByStudent_IdAndStop_IsNullOrderByCreatedAtAsc(1L))
            .thenReturn(List.of());
        when(notebookRepository.findByStudent_IdAndStop_IsNotNullOrderByStop_OrderIndexAscCreatedAtAsc(1L))
            .thenReturn(List.of());
        assertThat(notebookService.getEntries(1L)).isEmpty();
    }

    @Test
    void getEntries_mapsToDtoCorrectly() {
        Stop stop = makeStop(5L, 1);
        NotebookEntry entry = makeEntry(42L, stop, NotebookEntry.EntryType.AUTO_TIP, "Falske nyheter...");

        when(notebookRepository.findByStudent_IdAndStop_IsNullOrderByCreatedAtAsc(1L))
            .thenReturn(List.of());
        when(notebookRepository.findByStudent_IdAndStop_IsNotNullOrderByStop_OrderIndexAscCreatedAtAsc(1L))
            .thenReturn(List.of(entry));

        List<NotebookEntryDto> result = notebookService.getEntries(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(42L);
        assertThat(result.get(0).stopName()).isEqualTo("Stop5");
        assertThat(result.get(0).entryType()).isEqualTo("AUTO_TIP");
    }

    // ── getEntriesForTeacher ────────────────────────────────────

    @Test
    void getEntriesForTeacher_returnsEntriesWhenAuthorized() {
        Stop stop = makeStop(1L, 1);
        when(classroomStudentRepository.existsStudentInTeacherClassroom(10L, 5L)).thenReturn(true);
        when(notebookRepository.findByStudent_IdAndStop_IsNullOrderByCreatedAtAsc(5L))
            .thenReturn(List.of());
        when(notebookRepository.findByStudent_IdAndStop_IsNotNullOrderByStop_OrderIndexAscCreatedAtAsc(5L))
            .thenReturn(List.of(makeEntry(1L, stop, NotebookEntry.EntryType.AUTO_TIP, "tip")));

        List<NotebookEntryDto> result = notebookService.getEntriesForTeacher(10L, 5L);

        assertThat(result).hasSize(1);
    }

    @Test
    void getEntriesForTeacher_throwsForbiddenWhenNotAuthorized() {
        when(classroomStudentRepository.existsStudentInTeacherClassroom(10L, 5L)).thenReturn(false);
        assertThatThrownBy(() -> notebookService.getEntriesForTeacher(10L, 5L))
            .isInstanceOf(ResponseStatusException.class);
    }

    // ── createAutoTipIfNotExists ────────────────────────────────

    @Test
    void createAutoTipIfNotExists_savesEntryWhenNoneExists() {
        Stop stop = makeStop(1L, 1);
        stop.setAutoTip("Falske nyheter...");

        when(notebookRepository.existsByStudent_IdAndStop_IdAndEntryType(
            1L, 1L, NotebookEntry.EntryType.AUTO_TIP)).thenReturn(false);
        when(userRepository.getReferenceById(1L)).thenReturn(new User());

        notebookService.createAutoTipIfNotExists(1L, stop);

        ArgumentCaptor<NotebookEntry> captor = ArgumentCaptor.forClass(NotebookEntry.class);
        verify(notebookRepository).save(captor.capture());
        assertThat(captor.getValue().getEntryType()).isEqualTo(NotebookEntry.EntryType.AUTO_TIP);
        assertThat(captor.getValue().getContent()).isEqualTo("Falske nyheter...");
    }

    @Test
    void createAutoTipIfNotExists_skipsWhenAlreadyExists() {
        Stop stop = makeStop(1L, 1);
        stop.setAutoTip("tip");

        when(notebookRepository.existsByStudent_IdAndStop_IdAndEntryType(
            1L, 1L, NotebookEntry.EntryType.AUTO_TIP)).thenReturn(true);

        notebookService.createAutoTipIfNotExists(1L, stop);

        verify(notebookRepository, never()).save(any());
    }

    @Test
    void createAutoTipIfNotExists_skipsWhenNoTipContent() {
        Stop stop = makeStop(1L, 1);
        stop.setAutoTip(null);
        notebookService.createAutoTipIfNotExists(1L, stop);
        verify(notebookRepository, never()).save(any());
    }

    @Test
    void createAutoClueIfNotExists_savesClueTextWhenNoneExists() {
        Stop stop = makeStop(1L, 1);
        stop.setClueText("Tyven hadde røde sko.");

        when(notebookRepository.existsByStudent_IdAndStop_IdAndEntryType(
            1L, 1L, NotebookEntry.EntryType.AUTO_TIP)).thenReturn(false);
        when(userRepository.getReferenceById(1L)).thenReturn(new User());

        notebookService.createAutoClueIfNotExists(1L, stop);

        ArgumentCaptor<NotebookEntry> captor = ArgumentCaptor.forClass(NotebookEntry.class);
        verify(notebookRepository).save(captor.capture());
        assertThat(captor.getValue().getEntryType()).isEqualTo(NotebookEntry.EntryType.AUTO_TIP);
        assertThat(captor.getValue().getContent()).isEqualTo("Tyven hadde røde sko.");
    }

    // ── createReflection ───────────────────────────────────────

    @Test
    void createReflection_savesWhenStopCompleted() {
        Stop stop = makeStop(1L, 1);
        when(stopRepository.findById(1L)).thenReturn(Optional.of(stop));
        when(notebookRepository.existsByStudent_IdAndStop_IdAndEntryType(
            1L, 1L, NotebookEntry.EntryType.AUTO_TIP)).thenReturn(true);
        when(userRepository.getReferenceById(1L)).thenReturn(new User());

        NotebookEntry saved = makeEntry(99L, stop, NotebookEntry.EntryType.REFLECTION, "Min observasjon");
        when(notebookRepository.save(any())).thenReturn(saved);

        NotebookEntryDto result = notebookService.createReflection(1L, 1L, "Min observasjon");

        assertThat(result.id()).isEqualTo(99L);
        assertThat(result.entryType()).isEqualTo("REFLECTION");
    }

    @Test
    void createReflection_throwsForbiddenWhenStopNotCompleted() {
        Stop stop = makeStop(1L, 1);
        when(stopRepository.findById(1L)).thenReturn(Optional.of(stop));
        when(notebookRepository.existsByStudent_IdAndStop_IdAndEntryType(
            1L, 1L, NotebookEntry.EntryType.AUTO_TIP)).thenReturn(false);

        assertThatThrownBy(() -> notebookService.createReflection(1L, 1L, "text"))
            .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void createReflection_throwsNotFoundWhenStopMissing() {
        when(stopRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> notebookService.createReflection(1L, 99L, "text"))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
