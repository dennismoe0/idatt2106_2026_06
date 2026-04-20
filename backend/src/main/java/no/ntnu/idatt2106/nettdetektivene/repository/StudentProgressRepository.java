package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.StudentProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface StudentProgressRepository extends JpaRepository<StudentProgress, Long> {

    Optional<StudentProgress> findByStudent_IdAndTask_Id(Long studentId, Long taskId);

    long countByStudent_IdAndTask_Stop_IdAndCompletedTrue(Long studentId, Long stopId);

    @Query("SELECT COUNT(DISTINCT sp.stop.id) FROM StudentProgress sp WHERE sp.student.id = :studentId AND sp.completed = true")
    long countDistinctCompletedStops(@Param("studentId") Long studentId);
}
