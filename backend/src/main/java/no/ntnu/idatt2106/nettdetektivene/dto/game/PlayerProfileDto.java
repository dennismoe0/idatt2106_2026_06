package no.ntnu.idatt2106.nettdetektivene.dto.game;

/**
 * Response carrying a student's current level, XP, and star balance for the profile display.
 */
public record PlayerProfileDto(
    int level,
    int xp,
    int starBalance
) {}
