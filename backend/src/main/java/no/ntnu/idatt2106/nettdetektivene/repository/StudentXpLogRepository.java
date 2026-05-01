package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.StudentXpLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository for the {@link StudentXpLog} aggregate root.
 */
public interface StudentXpLogRepository extends JpaRepository<StudentXpLog, Long> {

    /**
     * Returns the most recently created XP log entry for the given student and stop.
     * Used to detect whether XP has already been awarded for completing a stop,
     * preventing duplicate grants.
     *
     * @param studentId the student user id
     * @param stopId    the stop id
     */
    Optional<StudentXpLog> findTopByStudent_IdAndStop_IdOrderByAwardedAtDesc(Long studentId, Long stopId);
}
