package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.StudentMysteryCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository for the {@link StudentMysteryCompletion} aggregate root, tracking
 * student attempts at the weekly mystery challenge.
 */
public interface StudentMysteryCompletionRepository extends JpaRepository<StudentMysteryCompletion, Long> {

    /**
     * Checks whether the student has already submitted an answer for the given mystery.
     *
     * @param studentId the student user id
     * @param mysteryId the weekly mystery id
     */
    boolean existsByStudentIdAndMysteryId(Long studentId, Long mysteryId);

    /**
     * Returns the number of mysteries the student has answered correctly.
     */
    long countByStudentIdAndCorrectTrue(Long studentId);

    /**
     * Returns the completion record for a specific student–mystery pair.
     *
     * @param studentId the student user id
     * @param mysteryId the weekly mystery id
     */
    Optional<StudentMysteryCompletion> findByStudentIdAndMysteryId(Long studentId, Long mysteryId);
}
