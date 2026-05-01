package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.NotebookEntry;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for the {@link NotebookEntry} aggregate root.
 */
public interface NotebookRepository extends JpaRepository<NotebookEntry, Long> {

    /**
     * Returns all stop-linked notebook entries for the given student, ordered by
     * the stop's display position and then chronologically. The stop association
     * is eagerly fetched to avoid N+1 queries.
     *
     * @param studentId the student user id
     */
    @EntityGraph(attributePaths = "stop")
    List<NotebookEntry> findByStudent_IdAndStop_IsNotNullOrderByStop_OrderIndexAscCreatedAtAsc(Long studentId);

    /**
     * Returns all notebook entries that are not linked to any stop (i.e. general
     * notes), ordered chronologically.
     *
     * @param studentId the student user id
     */
    List<NotebookEntry> findByStudent_IdAndStop_IsNullOrderByCreatedAtAsc(Long studentId);

    /**
     * Checks whether a particular entry type already exists for a given student and stop,
     * used to prevent duplicate auto-generated tips or clues.
     *
     * @param studentId the student user id
     * @param stopId    the stop id
     * @param entryType the entry type to check for
     */
    boolean existsByStudent_IdAndStop_IdAndEntryType(
            Long studentId, Long stopId, NotebookEntry.EntryType entryType);
}
