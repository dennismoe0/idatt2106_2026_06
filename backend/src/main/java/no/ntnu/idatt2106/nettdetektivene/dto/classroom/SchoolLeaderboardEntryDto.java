package no.ntnu.idatt2106.nettdetektivene.dto.classroom;

import no.ntnu.idatt2106.nettdetektivene.dto.avatar.AvatarResponse;

public record SchoolLeaderboardEntryDto(
    long studentId,
    String displayName,
    long classroomId,
    String classroomName,
    int completedTasks,
    int totalTasks,
    AvatarResponse avatar
) {}
