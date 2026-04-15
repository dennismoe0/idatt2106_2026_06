package no.ntnu.idatt2106.nettdetektivene.dto.game;

public record SubmitAnswerResponse(
    boolean correct,
    int score,
    String explanation,
    boolean stopCompleted,
    MedalDto medalEarned
) {}
