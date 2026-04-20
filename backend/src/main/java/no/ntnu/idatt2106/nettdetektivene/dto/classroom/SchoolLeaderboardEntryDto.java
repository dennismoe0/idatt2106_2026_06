package no.ntnu.idatt2106.nettdetektivene.dto.classroom;

public record SchoolLeaderboardEntryDto(
    String displayName,
    long classroomId,
    String classroomName,
    int completedTasks,
    int totalTasks
) {}
