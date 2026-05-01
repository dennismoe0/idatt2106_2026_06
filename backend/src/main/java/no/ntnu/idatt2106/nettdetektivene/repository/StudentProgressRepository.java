package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.StudentProgress;
import no.ntnu.idatt2106.nettdetektivene.entity.TaskType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

/**
 * Repository for the {@link StudentProgress} aggregate root, tracking per-task
 * completion state for each student.
 */
public interface StudentProgressRepository extends JpaRepository<StudentProgress, Long> {

    /**
     * Finds the progress record for a specific student–task pair.
     *
     * @param studentId the student user id
     * @param taskId    the task id
     */
    Optional<StudentProgress> findByStudent_IdAndTask_Id(Long studentId, Long taskId);

    /**
     * Counts how many tasks the student has completed within the given stop.
     *
     * @param studentId the student user id
     * @param stopId    the stop id
     */
    long countByStudent_IdAndTask_Stop_IdAndCompletedTrue(Long studentId, Long stopId);

    /**
     * Counts completed tasks for a student within a stop, excluding tasks of the
     * specified types. Used to determine whether a stop is "done" while ignoring
     * non-scored task types such as {@code LEARN} and {@code CLUE_RIDDLE}.
     *
     * @param studentId  the student user id
     * @param stopId     the stop id
     * @param taskTypes  task types to exclude from the count
     */
    long countByStudent_IdAndTask_Stop_IdAndCompletedTrueAndTask_TaskTypeNotIn(
        Long studentId,
        Long stopId,
        Collection<TaskType> taskTypes
    );

    /**
     * Counts the number of distinct stops that the student has at least one
     * completed task in.
     *
     * @param studentId the student user id
     */
    @Query("SELECT COUNT(DISTINCT sp.stop.id) FROM StudentProgress sp WHERE sp.student.id = :studentId AND sp.completed = true")
    long countDistinctCompletedStops(@Param("studentId") Long studentId);

    /**
     * Counts the total number of tasks the student has completed across all stops.
     */
    long countByStudent_IdAndCompletedTrue(Long studentId);

}
