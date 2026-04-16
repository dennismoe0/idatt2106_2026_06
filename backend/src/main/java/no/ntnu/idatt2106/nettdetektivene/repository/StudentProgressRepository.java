package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.StudentProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentProgressRepository extends JpaRepository<StudentProgress, Long> {
    List<StudentProgress> findByStudent_IdAndClassroom_Id(Long studentId, Long classroomId);

    Optional<StudentProgress> findByStudent_IdAndTask_IdAndClassroom_Id(
        Long studentId,
        Long taskId,
        Long classroomId
    );

    long countByStudent_IdAndTask_Stop_IdAndClassroom_IdAndCompletedTrue(
        Long studentId,
        Long stopId,
        Long classroomId
    );
}
