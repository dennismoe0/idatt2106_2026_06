package no.ntnu.idatt2106.nettdetektivene.dto;

public record WeeklyMysteryEditDto(
    String title,
    String description,
    String imageUrl,
    String mysteryType,
    String questionText,
    String correctAnswer,
    String teacherComment,
    int rewardStars,
    int rewardXp
) {}
