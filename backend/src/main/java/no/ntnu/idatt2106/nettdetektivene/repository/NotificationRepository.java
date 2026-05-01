package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for the {@link Notification} aggregate root.
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Returns all notifications for the given teacher, most recent first.
     */
    List<Notification> findByTeacher_IdOrderByCreatedAtDesc(Long teacherId);

    /**
     * Finds a specific notification by id, scoped to the given teacher to
     * prevent cross-teacher access.
     *
     * @param id        the notification id
     * @param teacherId the teacher user id that owns the notification
     */
    Optional<Notification> findByIdAndTeacher_Id(Long id, Long teacherId);

    /**
     * Returns the count of unread notifications for the given teacher.
     */
    long countByTeacher_IdAndIsReadFalse(Long teacherId);

    /**
     * Deletes all notifications for a teacher that were created before the given
     * timestamp. Used for scheduled clean-up of old notifications.
     *
     * @param teacherId the teacher user id
     * @param before    the cutoff timestamp; notifications older than this are removed
     * @return the number of rows deleted
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.teacher.id = :teacherId AND n.createdAt < :before")
    int deleteByTeacher_IdAndCreatedAtBefore(@Param("teacherId") Long teacherId,
        @Param("before") LocalDateTime before);
}