package no.ntnu.idatt2106.nettdetektivene.dto.avatar;

import jakarta.validation.constraints.NotBlank;

public record PurchaseItemRequest(
    @NotBlank String optionType,
    @NotBlank String optionValue
) {}
