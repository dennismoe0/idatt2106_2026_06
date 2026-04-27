package no.ntnu.idatt2106.nettdetektivene.service.answer;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.ntnu.idatt2106.nettdetektivene.entity.Task;
import no.ntnu.idatt2106.nettdetektivene.entity.TaskType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordTaskAnswerCheckerTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final PasswordTaskAnswerChecker checker =
        new PasswordTaskAnswerChecker(mapper, new PasswordStrengthEvaluator());

    @Test
    void supportedType_isPassword() {
        assertThat(checker.supportedTaskType()).isEqualTo(TaskType.PASSWORD);
    }

    @Test
    void choice_matchingSelected_passes() throws Exception {
        var correct = mapper.readTree("{\"selected\": \"d\"}");

        assertThat(checker.isCorrect(task(1L, "{\"type\": \"CHOICE\"}"), correct, Map.of("selected", "d"))).isTrue();
    }

    @Test
    void choice_isCaseInsensitive() throws Exception {
        var correct = mapper.readTree("{\"selected\": \"D\"}");

        assertThat(checker.isCorrect(task(2L, "{\"type\": \"CHOICE\"}"), correct, Map.of("selected", "d"))).isTrue();
    }

    @Test
    void choice_defaultsToChoiceWhenTypeMissing() throws Exception {
        var correct = mapper.readTree("{\"selected\": \"safe\"}");

        assertThat(checker.isCorrect(task(3L, "{\"question\": \"Pick one\"}"), correct, Map.of("selected", "safe"))).isTrue();
    }

    @Test
    void choice_defaultsToChoiceWhenTypeIsUnknown() throws Exception {
        var correct = mapper.readTree("{\"selected\": \"safe\"}");

        assertThat(checker.isCorrect(task(13L, "{\"type\": \"MYSTERY\"}"), correct, Map.of("selected", "safe"))).isTrue();
    }

    @Test
    void choice_wrongSelected_fails() throws Exception {
        var correct = mapper.readTree("{\"selected\": \"d\"}");

        assertThat(checker.isCorrect(task(4L, "{\"type\": \"CHOICE\"}"), correct, Map.of("selected", "a"))).isFalse();
    }

    @Test
    void choice_missingSubmittedSelection_fails() throws Exception {
        var correct = mapper.readTree("{\"selected\": \"d\"}");

        assertThat(checker.isCorrect(task(5L, "{\"type\": \"CHOICE\"}"), correct, Map.of())).isFalse();
    }

    @Test
    void choice_missingCorrectSelection_fails() throws Exception {
        var correct = mapper.readTree("{}");

        assertThat(checker.isCorrect(task(6L, "{\"type\": \"CHOICE\"}"), correct, Map.of("selected", "d"))).isFalse();
    }

    @Test
    void builder_fullyGreenPassword_passes() throws Exception {
        var correct = mapper.readTree("{\"minStrength\": \"STRONG\"}");

        assertThat(checker.isCorrect(task(7L, "{\"type\": \"BUILDER\"}"), correct, Map.of("password", "Tiger!Mane#42"))).isTrue();
    }

    @Test
    void builder_passwordBelowRequiredStrength_fails() throws Exception {
        var correct = mapper.readTree("{\"minStrength\": \"STRONG\"}");

        assertThat(checker.isCorrect(task(8L, "{\"type\": \"BUILDER\"}"), correct, Map.of("password", "abc123"))).isFalse();
    }

    @Test
    void builder_requiresFullyGreenEvenWithLowerThreshold() throws Exception {
        var correct = mapper.readTree("{\"minStrength\": \"MEDIUM\"}");

        assertThat(checker.isCorrect(task(9L, "{\"type\": \"BUILDER\"}"), correct, Map.of("password", "Tiger42"))).isFalse();
    }

    @Test
    void builder_missingPassword_fails() throws Exception {
        var correct = mapper.readTree("{\"minStrength\": \"STRONG\"}");

        assertThat(checker.isCorrect(task(10L, "{\"type\": \"BUILDER\"}"), correct, Map.of())).isFalse();
    }

    @Test
    void builder_defaultsRequiredStrengthToStrong() throws Exception {
        var correct = mapper.readTree("{}");

        assertThat(checker.isCorrect(task(11L, "{\"type\": \"BUILDER\"}"), correct, Map.of("password", "abc123"))).isFalse();
        assertThat(checker.isCorrect(task(11L, "{\"type\": \"BUILDER\"}"), correct, Map.of("password", "Tiger!Mane#42"))).isTrue();
    }

    @Test
    void builder_passwordContainingConfiguredPitfall_fails() throws Exception {
        var correct = mapper.readTree("{\"minStrength\": \"STRONG\"}");

        assertThat(checker.isCorrect(
            task(12L, "{\"type\": \"BUILDER\", \"pitfalls\": [\"OlaErBest\", \"2005\", \"hund\"]}"),
            correct,
            Map.of("password", "Trygg!OlaErBest#42")
        )).isFalse();
    }

    @Test
    void builder_pitfallComparisonIsCaseInsensitive() throws Exception {
        var correct = mapper.readTree("{\"minStrength\": \"STRONG\"}");

        assertThat(checker.isCorrect(
            task(13L, "{\"type\": \"BUILDER\", \"pitfalls\": [\"OlaErBest\", \"2005\", \"hund\"]}"),
            correct,
            Map.of("password", "TRYGG!hund#42")
        )).isFalse();
    }

    @Test
    void builder_passwordAboveMaxLength_fails() throws Exception {
        var correct = mapper.readTree("{}");

        assertThat(checker.isCorrect(
            task(15L, "{\"type\": \"BUILDER\", \"maxLength\": 12}"),
            correct,
            Map.of("password", "Tiger!Mane#42")
        )).isFalse();
    }

    @Test
    void builder_passwordAboveCorrectAnswerMaxLength_fails() throws Exception {
        var correct = mapper.readTree("{\"maxLength\": 12}");

        assertThat(checker.isCorrect(
            task(16L, "{\"type\": \"BUILDER\"}"),
            correct,
            Map.of("password", "Tiger!Mane#42")
        )).isFalse();
    }

    @Test
    void builder_answerWithTooManyParts_fails() throws Exception {
        var correct = mapper.readTree("{}");

        assertThat(checker.isCorrect(
            task(17L, "{\"type\": \"BUILDER\", \"maxParts\": 3}"),
            correct,
            Map.of("password", "Tiger!Mane#42", "parts", java.util.List.of("Tiger", "!", "Mane", "#", "42"))
        )).isFalse();
    }

    @Test
    void builder_passwordWithTooManyConfiguredTileParts_fails() throws Exception {
        var correct = mapper.readTree("{}");

        assertThat(checker.isCorrect(
            task(18L, """
                {
                  "type": "BUILDER",
                  "words": ["Tiger", "Mane"],
                  "symbols": ["!", "#"],
                  "numbers": ["42"],
                  "maxParts": 3
                }
                """),
            correct,
            Map.of("password", "Tiger!Mane#42")
        )).isFalse();
    }

    @Test
    void builder_submittedPartsMustMatchPasswordAndConfiguredTiles() throws Exception {
        var correct = mapper.readTree("{}");

        assertThat(checker.isCorrect(
            task(20L, """
                {
                  "type": "BUILDER",
                  "words": ["Tiger", "Mane"],
                  "symbols": ["!"],
                  "numbers": ["42"],
                  "maxParts": 4
                }
                """),
            correct,
            Map.of("password", "TigerMane42!", "parts", java.util.List.of("TigerMane42!"))
        )).isFalse();
    }

    @Test
    void builder_unsegmentablePasswordCannotBypassMaxParts() throws Exception {
        var correct = mapper.readTree("{}");

        assertThat(checker.isCorrect(
            task(21L, """
                {
                  "type": "BUILDER",
                  "words": ["Tiger", "Mane"],
                  "symbols": ["!"],
                  "numbers": ["42"],
                  "maxParts": 4
                }
                """),
            correct,
            Map.of("password", "Random!Safe42")
        )).isFalse();
    }

    @Test
    void builder_passwordBelowFullyGreenByCharacterRule_fails() throws Exception {
        var correct = mapper.readTree("{}");

        assertThat(checker.isCorrect(
            task(19L, "{\"type\": \"BUILDER\"}"),
            correct,
            Map.of("password", "TigerManePizza")
        )).isFalse();
    }

    @Test
    void malformedContentJson_fails() throws Exception {
        var correct = mapper.readTree("{\"selected\": \"d\"}");

        assertThat(checker.isCorrect(task(14L, "{not-valid-json"), correct, Map.of("selected", "d"))).isFalse();
    }

    private Task task(Long id, String contentJson) {
        Task task = new Task();
        task.setId(id);
        task.setTaskType(TaskType.PASSWORD);
        task.setContentJson(contentJson);
        return task;
    }
}
