package no.ntnu.idatt2106.nettdetektivene.dto.game;

import java.util.List;

public record SubmitAnswerResponse(
    boolean correct,
    int score,
    String explanation,
    boolean stopCompleted,
    MedalDto medalEarned,
    int starsEarned,
    int xpEarned,
    List<String> correctClueIds,
    List<PhishingClueFeedbackDto> phishingClues,
    Integer correctArticleIndex,
    String clueText,
    boolean showSuspectReveal
) {}
