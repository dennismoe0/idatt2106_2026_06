package no.ntnu.idatt2106.nettdetektivene.dto.game;

public record StopResponse(
    Long id,
    String name,
    int orderIndex,
    String description,
    boolean locked,
    boolean completed,
    int taskCount,
    int correctCount,
    boolean xpClaimable
) {}
