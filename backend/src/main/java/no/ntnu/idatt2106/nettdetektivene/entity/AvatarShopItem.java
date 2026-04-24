package no.ntnu.idatt2106.nettdetektivene.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

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
