package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.Task;
import no.ntnu.idatt2106.nettdetektivene.entity.TaskType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * Repository for the {@link Task} aggregate root.
 */
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Returns all tasks for a stop, ordered by their display position and then by id
     * as a stable tie-breaker.
     *
     * @param stopId the stop id
     */
    List<Task> findByStop_IdOrderByOrderIndexAscIdAsc(Long stopId);

    /**
     * Returns the total number of tasks associated with the given stop.
     */
    long countByStop_Id(Long stopId);

    /**
     * Returns the number of tasks for a stop excluding those with the given task types.
     * Used alongside {@link StudentProgressRepository} to determine stop-completion
     * without counting non-scored task types.
     *
     * @param stopId    the stop id
     * @param taskTypes task types to exclude from the count
     */
    long countByStop_IdAndTaskTypeNotIn(Long stopId, Collection<TaskType> taskTypes);

    /**
     * Checks whether at least one task of the given type exists for the given stop.
     *
     * @param stopId   the stop id
     * @param taskType the task type to look for
     */
    boolean existsByStop_IdAndTaskType(Long stopId, TaskType taskType);
}
