package no.ntnu.idatt2106.nettdetektivene.dto.avatar;

import jakarta.validation.constraints.NotBlank;

public record UpdateAvatarRequest(
    @NotBlank String gender,
    @NotBlank String eyeColor,
    @NotBlank String skinColor,
    @NotBlank String hairColor,
    @NotBlank String hairStyle,
    @NotBlank String outfit,
    @NotBlank String outfitColor,
    String hatColor,
    String accessory
) {}
