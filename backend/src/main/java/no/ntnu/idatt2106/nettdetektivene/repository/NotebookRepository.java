package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.NotebookEntry;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotebookRepository extends JpaRepository<NotebookEntry, Long> {

    @EntityGraph(attributePaths = "stop")
    List<NotebookEntry> findByStudent_IdOrderByStop_OrderIndexAscCreatedAtAsc(Long studentId);

    boolean existsByStudent_IdAndStop_IdAndEntryType(
            Long studentId, Long stopId, NotebookEntry.EntryType entryType);
}
