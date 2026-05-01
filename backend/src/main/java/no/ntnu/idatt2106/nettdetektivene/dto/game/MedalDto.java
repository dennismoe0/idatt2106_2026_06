package no.ntnu.idatt2106.nettdetektivene.dto.game;

/**
 * Response representing a medal's definition (without timestamp), used inline in task submit responses.
 */
public record MedalDto(
    Long id,
    String name,
    String description,
    String imageUrl
) {}
