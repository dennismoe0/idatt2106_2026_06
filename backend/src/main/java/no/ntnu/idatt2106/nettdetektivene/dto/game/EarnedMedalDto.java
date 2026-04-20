package no.ntnu.idatt2106.nettdetektivene.dto.game;

import java.time.LocalDateTime;

public record EarnedMedalDto(
    Long id,
    String name,
    String description,
    String imageUrl,
    LocalDateTime earnedAt
) {}
