package no.ntnu.idatt2106.nettdetektivene.service.answer;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.ntnu.idatt2106.nettdetektivene.entity.Task;
import no.ntnu.idatt2106.nettdetektivene.entity.TaskType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LearningTaskAnswerCheckerTest {

    private final LearningTaskAnswerChecker checker = new LearningTaskAnswerChecker();
    private final ObjectMapper mapper = new ObjectMapper();

    private Task task(long id) {
        Task t = new Task();
        t.setId(id);
        return t;
    }

    @Test
    void supportedType_isLearn() {
        assertThat(checker.supportedTaskType()).isEqualTo(TaskType.LEARN);
    }

    @Test
    void isCorrect_alwaysTrue_forAnyAnswer() throws Exception {
        var correctAnswer = mapper.readTree("{\"quizPassed\": true}");
        assertThat(checker.isCorrect(task(1), correctAnswer, Map.of("quizPassed", true))).isTrue();
    }

    @Test
    void isCorrect_alwaysTrue_forEmptyAnswer() throws Exception {
        var correctAnswer = mapper.readTree("{}");
        assertThat(checker.isCorrect(task(2), correctAnswer, Map.of())).isTrue();
    }
}
