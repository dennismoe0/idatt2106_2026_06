package no.ntnu.idatt2106.nettdetektivene.dto.classroom;

/**
 * Response representing a single student's ranking entry on the classroom leaderboard.
 */
public record LeaderboardEntryDto(
    String displayName,
    int completedTasks,
    int totalTasks
) {}
