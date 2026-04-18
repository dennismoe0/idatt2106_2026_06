package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.StudentXpLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StudentXpLogRepository extends JpaRepository<StudentXpLog, Long> {
    Optional<StudentXpLog> findTopByStudent_IdAndStop_IdOrderByAwardedAtDesc(Long studentId, Long stopId);
}
