package no.ntnu.idatt2106.nettdetektivene.dto.classroom;

public record LeaderboardEntryDto(
    String displayName,
    int completedTasks,
    int totalTasks
) {}
