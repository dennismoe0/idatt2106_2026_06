package no.ntnu.idatt2106.nettdetektivene.service.answer;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.ntnu.idatt2106.nettdetektivene.entity.TaskType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MarketplaceTaskAnswerCheckerTest {

    private final MarketplaceTaskAnswerChecker checker = new MarketplaceTaskAnswerChecker();
    private final ObjectMapper mapper = new ObjectMapper();

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
    void clickSuspicious_missingElement_fails() throws Exception {
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
}
