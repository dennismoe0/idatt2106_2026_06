package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.AvatarShopItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AvatarShopItemRepository extends JpaRepository<AvatarShopItem, Long> {
    List<AvatarShopItem> findAllByOrderByDisplayOrderAsc();
    Optional<AvatarShopItem> findByOptionTypeAndOptionValue(String optionType, String optionValue);
}
