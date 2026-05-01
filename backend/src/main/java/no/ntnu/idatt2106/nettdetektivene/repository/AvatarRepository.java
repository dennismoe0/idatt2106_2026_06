package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.Avatar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository for the {@link Avatar} aggregate root.
 */
@Repository
public interface AvatarRepository extends JpaRepository<Avatar, Long> {

    /**
     * Finds the avatar belonging to the given student.
     *
     * @param userId the id of the student user
     * @return the avatar, or empty if the student has not yet been given one
     */
    Optional<Avatar> findByStudent_Id(Long userId);
}
