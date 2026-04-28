package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.Task;
import no.ntnu.idatt2106.nettdetektivene.entity.TaskType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByStop_IdOrderByOrderIndexAscIdAsc(Long stopId);
    long countByStop_Id(Long stopId);
    long countByStop_IdAndTaskTypeNotIn(Long stopId, Collection<TaskType> taskTypes);
}
