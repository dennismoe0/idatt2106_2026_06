package no.ntnu.idatt2106.nettdetektivene.service.answer;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.ntnu.idatt2106.nettdetektivene.entity.TaskType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ClueRiddleTaskAnswerCheckerTest {

    private final ClueRiddleTaskAnswerChecker checker = new ClueRiddleTaskAnswerChecker();
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void supportedType_isClueRiddle() {
        assertThat(checker.supportedTaskType()).isEqualTo(TaskType.CLUE_RIDDLE);
    }

    @Test
    void matchingSelectedOption_passes() throws Exception {
        var correct = mapper.readTree("{\"selected\":\"cafe_admin\"}");

        assertThat(checker.isCorrect(null, correct, Map.of("selected", "cafe_admin"))).isTrue();
    }

    @Test
    void wrongSelectedOption_fails() throws Exception {
        var correct = mapper.readTree("{\"selected\":\"cafe_admin\"}");

        assertThat(checker.isCorrect(null, correct, Map.of("selected", "wrong_domain"))).isFalse();
    }

    @Test
    void missingSelectedOption_fails() throws Exception {
        var correct = mapper.readTree("{\"selected\":\"cafe_admin\"}");

        assertThat(checker.isCorrect(null, correct, Map.of())).isFalse();
    }

    @Test
    void nullSelectedOption_fails() throws Exception {
        var correct = mapper.readTree("{\"selected\":\"cafe_admin\"}");
        var answer = new HashMap<String, Object>();
        answer.put("selected", null);

        assertThat(checker.isCorrect(null, correct, answer)).isFalse();
    }
}
