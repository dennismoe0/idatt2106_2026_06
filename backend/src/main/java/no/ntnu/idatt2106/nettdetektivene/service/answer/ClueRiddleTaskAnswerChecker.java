package no.ntnu.idatt2106.nettdetektivene.service.answer;

import com.fasterxml.jackson.databind.JsonNode;
import no.ntnu.idatt2106.nettdetektivene.entity.Task;
import no.ntnu.idatt2106.nettdetektivene.entity.TaskType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ClueRiddleTaskAnswerChecker implements TaskAnswerChecker {

    @Override
    public TaskType supportedTaskType() {
        return TaskType.CLUE_RIDDLE;
    }

    @Override
    public boolean isCorrect(Task task, JsonNode correctAnswer, Map<String, Object> answer) {
        Object submitted = answer.get("selected");
        if (submitted == null || correctAnswer.path("selected").isMissingNode()) {
            return false;
        }
        return correctAnswer.path("selected").asText().equalsIgnoreCase(String.valueOf(submitted));
    }
}
