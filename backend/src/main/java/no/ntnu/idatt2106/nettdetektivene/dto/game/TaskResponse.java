package no.ntnu.idatt2106.nettdetektivene.dto.game;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Response carrying all data needed to render a task, including its type, content JSON, and the parent stop's context.
 */
public record TaskResponse(
    Long id,
    Long stopId,
    String stopName,
    String stopDescription,
    int stopOrderIndex,
    String stopTheme,
    String taskType,
    JsonNode contentJson,
    String guidanceText,
    boolean alreadyCompleted
) {}
