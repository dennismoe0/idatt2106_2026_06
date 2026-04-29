package no.ntnu.idatt2106.nettdetektivene.service.answer;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.ntnu.idatt2106.nettdetektivene.entity.Task;
import no.ntnu.idatt2106.nettdetektivene.entity.TaskType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MarketplaceTaskAnswerCheckerTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final MarketplaceTaskAnswerChecker checker = new MarketplaceTaskAnswerChecker(mapper);

    @Test
    void supportedType_isMarketplace() {
        assertThat(checker.supportedTaskType()).isEqualTo(TaskType.MARKETPLACE);
    }

    // ── CLICK_SUSPICIOUS branch ──────────────────────────────────────────────

    @Test
    void clickSuspicious_exactMatch_passes() throws Exception {
        var correct = mapper.readTree("{\"correctElementIds\": [\"domain\", \"payment\"]}");
        var answer  = Map.<String, Object>of("flaggedElementIds", List.of("domain", "payment"));
        assertThat(checker.isCorrect(null, correct, answer)).isTrue();
    }

    @Test
    void clickSuspicious_orderIndependent_passes() throws Exception {
        var correct = mapper.readTree("{\"correctElementIds\": [\"domain\", \"payment\"]}");
        var answer  = Map.<String, Object>of("flaggedElementIds", List.of("payment", "domain"));
        assertThat(checker.isCorrect(null, correct, answer)).isTrue();
    }

    @Test
    void clickSuspicious_partialMatch_fails() throws Exception {
        var correct = mapper.readTree("{\"correctElementIds\": [\"domain\", \"payment\"]}");
        var answer  = Map.<String, Object>of("flaggedElementIds", List.of("domain"));
        assertThat(checker.isCorrect(null, correct, answer)).isFalse();
    }

    @Test
    void clickSuspicious_extraElement_fails() throws Exception {
        var correct = mapper.readTree("{\"correctElementIds\": [\"domain\", \"payment\"]}");
        var answer  = Map.<String, Object>of("flaggedElementIds", List.of("domain", "payment", "contact"));
        assertThat(checker.isCorrect(null, correct, answer)).isFalse();
    }

    @Test
    void clickSuspicious_emptyCorrect_emptyFlagged_passes() throws Exception {
        var correct = mapper.readTree("{\"correctElementIds\": []}");
        var answer  = Map.<String, Object>of("flaggedElementIds", List.of());
        assertThat(checker.isCorrect(null, correct, answer)).isTrue();
    }

    @Test
    void clickSuspicious_emptyCorrect_someFlagged_fails() throws Exception {
        var correct = mapper.readTree("{\"correctElementIds\": []}");
        var answer  = Map.<String, Object>of("flaggedElementIds", List.of("domain"));
        assertThat(checker.isCorrect(null, correct, answer)).isFalse();
    }

    @Test
    void clickSuspicious_staleEmptyCorrect_fallsBackToContentJson_correctFlags() throws Exception {
        // Simulates stale DB: correctAnswerJson has empty correctElementIds,
        // but contentJson has isSuspicious: true on "domain" and "payment"
        var correct = mapper.readTree("{\"correctElementIds\": []}");
        var answer  = Map.<String, Object>of("flaggedElementIds", List.of("domain", "payment"));
        Task task = mock(Task.class);
        when(task.getContentJson()).thenReturn("""
            {"elements": [
              {"id": "domain",  "isSuspicious": true},
              {"id": "payment", "isSuspicious": true},
              {"id": "price",   "isSuspicious": false}
            ]}""");
        assertThat(checker.isCorrect(task, correct, answer)).isTrue();
    }

    @Test
    void clickSuspicious_staleEmptyCorrect_nothingFlagged_fails() throws Exception {
        // "Ingenting mistenkelig" when there are actually suspicious items
        var correct = mapper.readTree("{\"correctElementIds\": []}");
        var answer  = Map.<String, Object>of("flaggedElementIds", List.of());
        Task task = mock(Task.class);
        when(task.getContentJson()).thenReturn("""
            {"elements": [
              {"id": "domain",  "isSuspicious": true},
              {"id": "payment", "isSuspicious": true}
            ]}""");
        assertThat(checker.isCorrect(task, correct, answer)).isFalse();
    }

    @Test
    void clickSuspicious_missingFlaggedElementIds_fails() throws Exception {
        var correct = mapper.readTree("{\"correctElementIds\": [\"domain\"]}");
        assertThat(checker.isCorrect(null, correct, Map.of())).isFalse();
    }

    @Test
    void clickSuspicious_nonIterableFlaggedElementIds_fails() throws Exception {
        var correct = mapper.readTree("{\"correctElementIds\": [\"domain\"]}");
        var answer  = Map.<String, Object>of("flaggedElementIds", "domain");
        assertThat(checker.isCorrect(null, correct, answer)).isFalse();
    }

    @Test
    void clickSuspicious_duplicateIds_areDeduplicatedBeforeComparison() throws Exception {
        var correct = mapper.readTree("{\"correctElementIds\": [\"domain\", \"domain\"]}");
        var answer  = Map.<String, Object>of("flaggedElementIds", List.of("domain", "domain"));
        assertThat(checker.isCorrect(null, correct, answer)).isTrue();
    }

    @Test
    void clickSuspicious_numericIds_areNormalizedToStringBeforeComparison() throws Exception {
        var correct = mapper.readTree("{\"correctElementIds\": [1]}");
        var answer  = Map.<String, Object>of("flaggedElementIds", List.of(1));
        assertThat(checker.isCorrect(null, correct, answer)).isTrue();
    }

    @Test
    void clickSuspicious_takesPriorityOverLegacySelected() throws Exception {
        var correct = mapper.readTree("{\"correctElementIds\": [\"domain\"], \"selected\": \"wrong\"}");
        var answer  = Map.<String, Object>of(
            "flaggedElementIds", List.of("domain"),
            "selected", "wrong"
        );
        assertThat(checker.isCorrect(null, correct, answer)).isTrue();
    }

    // ── Legacy branch ────────────────────────────────────────────────────────

    @Test
    void legacy_matchingSelected_passes() throws Exception {
        var correct = mapper.readTree("{\"selected\": \"b\"}");
        assertThat(checker.isCorrect(null, correct, Map.of("selected", "b"))).isTrue();
    }

    @Test
    void legacy_caseInsensitive_passes() throws Exception {
        var correct = mapper.readTree("{\"selected\": \"B\"}");
        assertThat(checker.isCorrect(null, correct, Map.of("selected", "b"))).isTrue();
    }

    @Test
    void legacy_wrongSelected_fails() throws Exception {
        var correct = mapper.readTree("{\"selected\": \"b\"}");
        assertThat(checker.isCorrect(null, correct, Map.of("selected", "a"))).isFalse();
    }

    @Test
    void legacy_missingSelected_fails() throws Exception {
        var correct = mapper.readTree("{\"selected\": \"b\"}");
        assertThat(checker.isCorrect(null, correct, Map.of())).isFalse();
    }

    @Test
    void legacy_missingSelectedInCorrectAnswer_fails() throws Exception {
        var correct = mapper.readTree("{\"other\": \"b\"}");
        assertThat(checker.isCorrect(null, correct, Map.of("selected", "b"))).isFalse();
    }
}
