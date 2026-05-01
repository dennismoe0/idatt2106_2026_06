package no.ntnu.idatt2106.nettdetektivene.dto.game;

import java.util.Map;

/**
 * Request payload for submitting a student's answer to a task; the answer structure varies by task type.
 */
public record SubmitAnswerRequest(
    Map<String, Object> answer
) {}
