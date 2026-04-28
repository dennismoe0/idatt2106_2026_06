package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByTeacher_IdOrderByCreatedAtDesc(Long teacherId);

    Optional<Notification> findByIdAndTeacher_Id(Long id, Long teacherId);

    long countByTeacher_IdAndIsReadFalse(Long teacherId);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.teacher.id = :teacherId AND n.createdAt < :before")
    int deleteByTeacher_IdAndCreatedAtBefore(@Param("teacherId") Long teacherId,
        @Param("before") LocalDateTime before);
}