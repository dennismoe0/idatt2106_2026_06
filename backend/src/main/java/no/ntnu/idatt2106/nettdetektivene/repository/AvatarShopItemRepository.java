package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.AvatarShopItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for the {@link AvatarShopItem} aggregate root.
 */
public interface AvatarShopItemRepository extends JpaRepository<AvatarShopItem, Long> {

    /**
     * Returns all shop items sorted by their configured display order.
     */
    List<AvatarShopItem> findAllByOrderByDisplayOrderAsc();

    /**
     * Looks up a specific shop item by its cosmetic category and value.
     *
     * @param optionType  the cosmetic category (e.g. "hairColor")
     * @param optionValue the specific value within that category (e.g. "#FF0099")
     * @return the matching shop item, or empty if not found
     */
    Optional<AvatarShopItem> findByOptionTypeAndOptionValue(String optionType, String optionValue);
}
