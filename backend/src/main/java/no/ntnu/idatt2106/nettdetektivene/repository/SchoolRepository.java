package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for the {@link School} aggregate root.
 */
public interface SchoolRepository extends JpaRepository<School, Long> {

    /**
     * Finds a school by its unique join code.
     *
     * @param joinCode the short human-readable code used by teachers to affiliate with a school
     */
    Optional<School> findByJoinCode(String joinCode);

    /**
     * Checks whether a school with the given join code already exists.
     */
    boolean existsByJoinCode(String joinCode);
}
