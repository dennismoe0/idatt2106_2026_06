package no.ntnu.idatt2106.nettdetektivene.dto.game;

import java.util.Map;

public record SubmitAnswerRequest(
    Map<String, Object> answer
) {}
