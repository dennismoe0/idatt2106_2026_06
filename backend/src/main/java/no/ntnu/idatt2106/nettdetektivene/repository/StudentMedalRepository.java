package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.StudentMedal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentMedalRepository extends JpaRepository<StudentMedal, Long> {
    List<StudentMedal> findByStudent_IdAndClassroom_Id(Long studentId, Long classroomId);
    boolean existsByStudent_IdAndMedal_Id(Long studentId, Long medalId);
    boolean existsByStudent_IdAndMedal_IdAndClassroom_Id(Long studentId, Long medalId, Long classroomId);
}
