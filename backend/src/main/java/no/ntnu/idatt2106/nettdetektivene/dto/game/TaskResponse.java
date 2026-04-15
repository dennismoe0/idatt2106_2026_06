package no.ntnu.idatt2106.nettdetektivene.dto.game;

public record TaskResponse(
    Long id,
    Long stopId,
    String taskType,
    String contentJson,
    String guidanceText,
    boolean alreadyCompleted
) {}
