package no.ntnu.idatt2106.nettdetektivene.service.answer;

import com.fasterxml.jackson.databind.JsonNode;
import no.ntnu.idatt2106.nettdetektivene.entity.Task;
import no.ntnu.idatt2106.nettdetektivene.entity.TaskType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FakeNewsTaskAnswerChecker implements TaskAnswerChecker {

    @Override
    public TaskType supportedTaskType() {
        return TaskType.FAKE_NEWS;
    }

    @Override
    public boolean isCorrect(Task task, JsonNode correctAnswer, Map<String, Object> answer) {
        // Student picks exactly one article as fake (submitted false).
        // Correct if that article is actually fake in the answer key — other
        // articles are ignored so tasks with multiple fakes work correctly.
        for (Map.Entry<String, Object> entry : answer.entrySet()) {
            Boolean submitted = asBoolean(entry.getValue());
            if (Boolean.FALSE.equals(submitted)) {
                JsonNode correct = correctAnswer.get(entry.getKey());
                return correct != null && !correct.asBoolean();
            }
        }
        return false;
    }

    private Boolean asBoolean(Object value) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (value instanceof String stringValue) {
            if ("true".equalsIgnoreCase(stringValue)) {
                return true;
            }
            if ("false".equalsIgnoreCase(stringValue)) {
                return false;
            }
        }
        return null;
    }
}
