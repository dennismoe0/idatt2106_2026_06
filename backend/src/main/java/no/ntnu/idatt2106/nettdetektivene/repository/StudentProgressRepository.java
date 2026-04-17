package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.StudentProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentProgressRepository extends JpaRepository<StudentProgress, Long> {

    Optional<StudentProgress> findByStudent_IdAndTask_Id(Long studentId, Long taskId);

    long countByStudent_IdAndTask_Stop_IdAndCompletedTrue(Long studentId, Long stopId);
}
