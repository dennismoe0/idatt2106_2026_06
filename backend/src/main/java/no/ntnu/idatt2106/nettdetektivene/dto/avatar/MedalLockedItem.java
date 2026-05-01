package no.ntnu.idatt2106.nettdetektivene.dto.avatar;

/**
 * Describes an avatar item that is locked behind earning the medal for a specific game stop.
 */
public record MedalLockedItem(
    String optionType,
    String optionValue,
    long stopId,
    String stopName
) {}
