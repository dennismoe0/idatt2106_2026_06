package no.ntnu.idatt2106.nettdetektivene.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

class PhishingAnswerChecker {

    private PhishingAnswerChecker() {}

    /**
     * Returns true when the submitted answer correctly identifies all required clues.
     * Supports two correctAnswer formats:
     *  - New:  { "clues": ["id1", "id2"] } — checked against submitted flaggedClueIds
     *  - Old:  { "action": "REPORT" }      — checked against submitted action (backward compat)
     */
    static boolean check(JsonNode correctAnswer, Map<String, Object> answer) {
        if (correctAnswer == null || answer == null) return false;

        // New format: clue ID list
        if (correctAnswer.has("clues") && correctAnswer.path("clues").isArray()) {
            List<String> required = requiredClueIds(correctAnswer);
            if (required.isEmpty()) return false;
            Object raw = answer.get("flaggedClueIds");
            if (!(raw instanceof List<?> list)) return false;
            List<String> flagged = list.stream().map(Object::toString).toList();
            return new HashSet<>(flagged).containsAll(required);
        }

        // Old format: action string
        if (correctAnswer.has("action")) {
            Object submittedAction = answer.get("action");
            if (submittedAction == null) return false;
            return correctAnswer.path("action").asText().equalsIgnoreCase(String.valueOf(submittedAction));
        }

        return false;
    }

    static List<String> requiredClueIds(JsonNode correctAnswer) {
        List<String> ids = new ArrayList<>();
        if (correctAnswer == null) return ids;
        JsonNode clues = correctAnswer.path("clues");
        if (clues.isArray()) {
            clues.forEach(node -> ids.add(node.asText()));
        }
        return ids;
    }
}
