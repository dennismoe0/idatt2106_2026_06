package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByTeacher_IdOrderByCreatedAtDesc(Long teacherId);

    Optional<Notification> findByIdAndTeacher_Id(Long id, Long teacherId);

    long countByTeacher_IdAndIsReadFalse(Long teacherId);
}
