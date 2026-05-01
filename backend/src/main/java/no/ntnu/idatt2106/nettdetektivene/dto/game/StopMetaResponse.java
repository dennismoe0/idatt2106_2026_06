package no.ntnu.idatt2106.nettdetektivene.dto.game;

/**
 * Response carrying lightweight metadata about a game stop (no lock/completion state) for listing purposes.
 */
public record StopMetaResponse(
    Long id,
    String name,
    int orderIndex,
    String theme,
    int taskCount
) {}
