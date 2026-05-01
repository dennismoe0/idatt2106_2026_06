package no.ntnu.idatt2106.nettdetektivene.dto;

/**
 * Request payload for a teacher to edit the details of a weekly mystery, including the correct answer and rewards.
 */
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
