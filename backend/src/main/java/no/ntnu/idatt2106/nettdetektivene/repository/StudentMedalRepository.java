package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.StudentMedal;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentMedalRepository extends JpaRepository<StudentMedal, Long> {
    @EntityGraph(attributePaths = "medal")
    List<StudentMedal> findByStudent_Id(Long studentId);
    boolean existsByStudent_IdAndMedal_Id(Long studentId, Long medalId);
}
