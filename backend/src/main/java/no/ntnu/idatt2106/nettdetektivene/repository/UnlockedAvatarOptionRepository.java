package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.UnlockedAvatarOption;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository for the {@link UnlockedAvatarOption} aggregate root, tracking
 * cosmetic options each student has unlocked.
 */
public interface UnlockedAvatarOptionRepository extends JpaRepository<UnlockedAvatarOption, Long> {

    /**
     * Returns all cosmetic options unlocked by the given student.
     *
     * @param studentId the student user id
     */
    List<UnlockedAvatarOption> findByStudentId(Long studentId);

    /**
     * Checks whether the student has already unlocked the given cosmetic option,
     * used to prevent double-granting on medal or purchase events.
     *
     * @param studentId   the student user id
     * @param optionType  the cosmetic category (e.g. "hairColor")
     * @param optionValue the specific value within that category
     */
    boolean existsByStudentIdAndOptionTypeAndOptionValue(Long studentId, String optionType, String optionValue);
}
