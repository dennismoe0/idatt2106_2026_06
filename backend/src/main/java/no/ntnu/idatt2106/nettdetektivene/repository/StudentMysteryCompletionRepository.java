package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.StudentMysteryCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StudentMysteryCompletionRepository extends JpaRepository<StudentMysteryCompletion, Long> {

    boolean existsByStudentIdAndMysteryId(Long studentId, Long mysteryId);

    long countByStudentIdAndCorrectTrue(Long studentId);

    Optional<StudentMysteryCompletion> findByStudentIdAndMysteryId(Long studentId, Long mysteryId);
}
