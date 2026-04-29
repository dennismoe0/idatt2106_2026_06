package no.ntnu.idatt2106.nettdetektivene.dto.game;

public record StopMetaResponse(
    Long id,
    String name,
    int orderIndex,
    String theme,
    int taskCount
) {}
