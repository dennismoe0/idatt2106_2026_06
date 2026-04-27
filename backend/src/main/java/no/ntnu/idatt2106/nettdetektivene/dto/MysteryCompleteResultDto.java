package no.ntnu.idatt2106.nettdetektivene.dto;

public record MysteryCompleteResultDto(
    boolean correct,
    String teacherComment,
    int starsEarned,
    int xpEarned,
    boolean medalEarned,
    String medalName
) {}
