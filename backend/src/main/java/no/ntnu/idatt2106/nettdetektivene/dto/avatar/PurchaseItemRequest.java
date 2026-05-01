package no.ntnu.idatt2106.nettdetektivene.dto.avatar;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for purchasing an avatar item from the shop using the student's star balance.
 */
public record PurchaseItemRequest(
    @NotBlank String optionType,
    @NotBlank String optionValue
) {}
