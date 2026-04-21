package no.ntnu.idatt2106.nettdetektivene.service.answer;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.ntnu.idatt2106.nettdetektivene.entity.TaskType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AiPhotoTaskAnswerCheckerTest {

    private final AiPhotoTaskAnswerChecker checker = new AiPhotoTaskAnswerChecker();
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void supportedType_isAiPhoto() {
        assertThat(checker.supportedTaskType()).isEqualTo(TaskType.AI_PHOTO);
    }

    // ── FIND_ARTIFACTS branch ────────────────────────────────────────────────

    @Test
    void findArtifacts_allFound_passes() throws Exception {
        var correct = mapper.readTree("{\"foundArtifactIds\": [\"logo\", \"text\"]}");
        var answer  = Map.<String, Object>of("foundArtifactIds", List.of("logo", "text"));
        assertThat(checker.isCorrect(null, correct, answer)).isTrue();
    }

    @Test
    void findArtifacts_orderIndependent_passes() throws Exception {
        var correct = mapper.readTree("{\"foundArtifactIds\": [\"logo\", \"text\"]}");
        var answer  = Map.<String, Object>of("foundArtifactIds", List.of("text", "logo"));
        assertThat(checker.isCorrect(null, correct, answer)).isTrue();
    }

    @Test
    void findArtifacts_allRequiredPlusSome_passes() throws Exception {
        // containsAll — extra finds are fine (partial credit / bonus taps)
        var correct = mapper.readTree("{\"foundArtifactIds\": [\"logo\"]}");
        var answer  = Map.<String, Object>of("foundArtifactIds", List.of("logo", "shadow"));
        assertThat(checker.isCorrect(null, correct, answer)).isTrue();
    }

    @Test
    void findArtifacts_missingRequired_fails() throws Exception {
        var correct = mapper.readTree("{\"foundArtifactIds\": [\"logo\", \"text\"]}");
        var answer  = Map.<String, Object>of("foundArtifactIds", List.of("logo"));
        assertThat(checker.isCorrect(null, correct, answer)).isFalse();
    }

    @Test
    void findArtifacts_noneFound_fails() throws Exception {
        var correct = mapper.readTree("{\"foundArtifactIds\": [\"logo\"]}");
        var answer  = Map.<String, Object>of("foundArtifactIds", List.of());
        assertThat(checker.isCorrect(null, correct, answer)).isFalse();
    }

    // ── Classify branch ──────────────────────────────────────────────────────

    @Test
    void classify_allMatch_passes() throws Exception {
        var correct = mapper.readTree("{\"image_0\": \"AI_GENERATED\", \"image_1\": \"REAL\"}");
        var answer  = Map.<String, Object>of("image_0", "AI_GENERATED", "image_1", "REAL");
        assertThat(checker.isCorrect(null, correct, answer)).isTrue();
    }

    @Test
    void classify_caseInsensitive_passes() throws Exception {
        var correct = mapper.readTree("{\"image_0\": \"AI_GENERATED\"}");
        var answer  = Map.<String, Object>of("image_0", "ai_generated");
        assertThat(checker.isCorrect(null, correct, answer)).isTrue();
    }

    @Test
    void classify_wrongLabel_fails() throws Exception {
        var correct = mapper.readTree("{\"image_0\": \"AI_GENERATED\"}");
        var answer  = Map.<String, Object>of("image_0", "REAL");
        assertThat(checker.isCorrect(null, correct, answer)).isFalse();
    }

    @Test
    void classify_missingKey_fails() throws Exception {
        var correct = mapper.readTree("{\"image_0\": \"REAL\", \"image_1\": \"AI_GENERATED\"}");
        var answer  = Map.<String, Object>of("image_0", "REAL");
        assertThat(checker.isCorrect(null, correct, answer)).isFalse();
    }
}
