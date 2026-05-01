package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.Stop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for the {@link Stop} aggregate root.
 */
public interface StopRepository extends JpaRepository<Stop, Long> {

    /**
     * Returns all stops ordered by their sequential position on the game map.
     */
    List<Stop> findAllByOrderByOrderIndexAsc();
}
