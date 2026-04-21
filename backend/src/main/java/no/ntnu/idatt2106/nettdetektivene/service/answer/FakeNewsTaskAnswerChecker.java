package no.ntnu.idatt2106.nettdetektivene.service.answer;

import com.fasterxml.jackson.databind.JsonNode;
import no.ntnu.idatt2106.nettdetektivene.entity.TaskType;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;

@Component
public class FakeNewsTaskAnswerChecker implements TaskAnswerChecker {

    @Override
    public TaskType supportedTaskType() {
        return TaskType.FAKE_NEWS;
    }

    @Override
    public boolean isCorrect(JsonNode correctAnswer, Map<String, Object> answer) {
        Iterator<Map.Entry<String, JsonNode>> fields = correctAnswer.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            Boolean submitted = asBoolean(answer.get(field.getKey()));
            if (submitted == null || submitted != field.getValue().asBoolean()) {
                return false;
            }
        }
        return true;
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
