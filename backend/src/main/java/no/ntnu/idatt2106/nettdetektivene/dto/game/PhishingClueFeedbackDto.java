package no.ntnu.idatt2106.nettdetektivene.dto.game;

/**
 * Response describing a single phishing clue element, indicating whether it is a genuine clue and providing an explanation.
 */
public record PhishingClueFeedbackDto(
    String id,
    String label,
    String explanation,
    boolean isClue
) {}
