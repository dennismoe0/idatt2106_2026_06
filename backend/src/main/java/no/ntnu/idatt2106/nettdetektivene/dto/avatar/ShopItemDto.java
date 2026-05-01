package no.ntnu.idatt2106.nettdetektivene.dto.avatar;

/**
 * Response representing a single purchasable avatar item in the shop, including its star price and purchase status.
 */
public record ShopItemDto(
    Long id,
    String optionType,
    String optionValue,
    int starPrice,
    boolean purchased
) {}
