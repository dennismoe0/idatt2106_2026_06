package no.ntnu.idatt2106.nettdetektivene.dto.avatar;

public record ShopItemDto(
    Long id,
    String optionType,
    String optionValue,
    int starPrice,
    boolean purchased
) {}
