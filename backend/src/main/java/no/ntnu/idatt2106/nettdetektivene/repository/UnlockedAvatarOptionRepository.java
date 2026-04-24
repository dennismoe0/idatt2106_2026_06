package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.UnlockedAvatarOption;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UnlockedAvatarOptionRepository extends JpaRepository<UnlockedAvatarOption, Long> {
    List<UnlockedAvatarOption> findByStudentId(Long studentId);
    boolean existsByStudentIdAndOptionTypeAndOptionValue(Long studentId, String optionType, String optionValue);
}
