package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.Medal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedalRepository extends JpaRepository<Medal, Long> {
    Optional<Medal> findByStop_Id(Long stopId);
    Optional<Medal> findByName(String name);
}
