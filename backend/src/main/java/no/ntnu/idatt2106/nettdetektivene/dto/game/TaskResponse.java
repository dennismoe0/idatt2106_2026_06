package no.ntnu.idatt2106.nettdetektivene.dto.game;

import com.fasterxml.jackson.databind.JsonNode;

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
