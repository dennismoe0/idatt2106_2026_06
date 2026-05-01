package no.ntnu.idatt2106.nettdetektivene.dto.classroom;

import no.ntnu.idatt2106.nettdetektivene.dto.avatar.AvatarResponse;

/**
 * Response representing a student's ranking entry on the school-wide leaderboard, including classroom and avatar details.
 */
public record SchoolLeaderboardEntryDto(
    long studentId,
    String displayName,
    long classroomId,
    String classroomName,
    String schoolName,
    int completedTasks,
    int totalTasks,
    AvatarResponse avatar
) {}
