package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.WeeklyMystery;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for the {@link WeeklyMystery} aggregate root.
 */
public interface WeeklyMysteryRepository extends JpaRepository<WeeklyMystery, Long> {

    /**
     * Returns all mysteries for the given classroom, newest first.
     *
     * @param classroomId the classroom id
     */
    List<WeeklyMystery> findByClassroomIdOrderByCreatedAtDesc(Long classroomId);

    /**
     * Returns all mysteries in the given classroom that have the specified moderation status.
     *
     * @param classroomId the classroom id
     * @param status      the required moderation status
     */
    List<WeeklyMystery> findByClassroomIdAndStatus(Long classroomId, WeeklyMystery.Status status);

    /**
     * Returns the currently featured mystery for the given classroom, if one is active.
     * Only one mystery per classroom may be featured at a time.
     *
     * @param classroomId the classroom id
     */
    Optional<WeeklyMystery> findByClassroomIdAndFeaturedTrue(Long classroomId);
}
