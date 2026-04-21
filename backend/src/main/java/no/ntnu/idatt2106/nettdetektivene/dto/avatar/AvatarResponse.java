package no.ntnu.idatt2106.nettdetektivene.dto.avatar;

public record AvatarResponse(
    String gender,
    String eyeColor,
    String eyeStyle,
    String skinColor,
    String hairColor,
    String hairStyle,
    String outfit,
    String outfitColor,
    String hatColor,
    String accessory
) {}
