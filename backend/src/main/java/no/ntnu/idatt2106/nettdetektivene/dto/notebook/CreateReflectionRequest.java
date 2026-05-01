package no.ntnu.idatt2106.nettdetektivene.dto.notebook;

/**
 * Request payload for saving a student's reflection tied to a specific game stop in their notebook.
 */
public record CreateReflectionRequest(
    Long stopId,
    String content
) {}
