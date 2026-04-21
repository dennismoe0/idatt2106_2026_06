package no.ntnu.idatt2106.nettdetektivene.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GameServicePhishingTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private JsonNode json(String raw) {
        try {
            return mapper.readTree(raw);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void check_returnsTrue_whenAllRequiredCluesFlagged() {
        JsonNode correct = json("""
            { "clues": ["sender", "link1"] }
            """);
        Map<String, Object> answer = Map.of("flaggedClueIds", List.of("sender", "link1", "urgency"));
        assertThat(PhishingAnswerChecker.check(correct, answer)).isTrue();
    }

    @Test
    void check_returnsFalse_whenRequiredCluesMissing() {
        JsonNode correct = json("""
            { "clues": ["sender", "link1", "urgency"] }
            """);
        Map<String, Object> answer = Map.of("flaggedClueIds", List.of("sender"));
        assertThat(PhishingAnswerChecker.check(correct, answer)).isFalse();
    }

    @Test
    void check_backwardCompat_legacyActionFormat() {
        JsonNode correct = json("""
            { "action": "REPORT" }
            """);
        Map<String, Object> answer = Map.of("action", "REPORT");
        assertThat(PhishingAnswerChecker.check(correct, answer)).isTrue();
    }

    @Test
    void requiredClueIds_returnsListFromCluesArray() {
        JsonNode correct = json("""
            { "clues": ["sender", "urgency"] }
            """);
        assertThat(PhishingAnswerChecker.requiredClueIds(correct))
            .containsExactlyInAnyOrder("sender", "urgency");
    }
}
