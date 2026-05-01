package no.ntnu.idatt2106.nettdetektivene.dto.game;

/**
 * Response representing a game stop with full student-specific state, including lock status, completion, and XP claim availability.
 */
public record StopResponse(
    Long id,
    String name,
    int orderIndex,
    String description,
    boolean locked,
    boolean completed,
    int taskCount,
    int correctCount,
    boolean xpClaimable,
    String theme
) {}
