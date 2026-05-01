package no.ntnu.idatt2106.nettdetektivene.dto;

/**
 * Response returned after a student submits their weekly mystery answer, including rewards and correctness feedback.
 */
public record MysteryCompleteResultDto(
    boolean correct,
    String teacherComment,
    int starsEarned,
    int xpEarned,
    boolean medalEarned,
    String medalName
) {}
