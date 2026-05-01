package no.ntnu.idatt2106.nettdetektivene.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * Represents a purchasable cosmetic item available in the in-game avatar shop.
 * Items are identified by an {@code optionType} (e.g. "hairColor") and
 * {@code optionValue} (e.g. "#FF0099"), and are priced in star currency.
 * The {@code displayOrder} field controls the order items appear in the shop UI.
 */
@Entity
@Table(name = "avatar_shop_items")
@Getter @Setter @NoArgsConstructor
public class AvatarShopItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "option_type", nullable = false, length = 50)
    private String optionType;

    @Column(name = "option_value", nullable = false, length = 100)
    private String optionValue;

    @Column(name = "star_price", nullable = false)
    private int starPrice;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;
}
