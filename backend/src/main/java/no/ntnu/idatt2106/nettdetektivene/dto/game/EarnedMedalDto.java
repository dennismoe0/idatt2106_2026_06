package no.ntnu.idatt2106.nettdetektivene.dto.game;

import java.time.LocalDateTime;

/**
 * Response representing a medal that a student has earned, including when it was awarded.
 */
public record EarnedMedalDto(
    Long id,
    String name,
    String description,
    String imageUrl,
    LocalDateTime earnedAt
) {}
