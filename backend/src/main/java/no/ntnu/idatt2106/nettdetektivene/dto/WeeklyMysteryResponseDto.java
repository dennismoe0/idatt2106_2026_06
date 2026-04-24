package no.ntnu.idatt2106.nettdetektivene.dto;

import java.time.LocalDateTime;

public record WeeklyMysteryResponseDto(
    Long id,
    String title,
    String description,
    String imageUrl,
    String status,
    boolean featured,
    String mysteryType,
    String questionText,
    String teacherComment,
    int rewardStars,
    int rewardXp,
    LocalDateTime createdAt,
    String submittedByDisplayName
) {}
