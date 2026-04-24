package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.WeeklyMystery;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WeeklyMysteryRepository extends JpaRepository<WeeklyMystery, Long> {

    List<WeeklyMystery> findByClassroomIdOrderByCreatedAtDesc(Long classroomId);

    List<WeeklyMystery> findByClassroomIdAndStatus(Long classroomId, WeeklyMystery.Status status);

    Optional<WeeklyMystery> findByClassroomIdAndFeaturedTrue(Long classroomId);
}
