package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.Medal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for the {@link Medal} aggregate root.
 */
public interface MedalRepository extends JpaRepository<Medal, Long> {

    /**
     * Returns the medal associated with the given stop, if any.
     *
     * @param stopId the stop id
     */
    Optional<Medal> findByStop_Id(Long stopId);

    /**
     * Returns the medal with the given name.
     *
     * @param name the medal name as defined in seed data
     */
    Optional<Medal> findByName(String name);
}
