package no.ntnu.idatt2106.nettdetektivene.service.answer;

import com.fasterxml.jackson.databind.JsonNode;
import no.ntnu.idatt2106.nettdetektivene.entity.Task;
import no.ntnu.idatt2106.nettdetektivene.entity.TaskType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FinalBossTaskAnswerChecker implements TaskAnswerChecker {

    @Override
    public TaskType supportedTaskType() {
        return TaskType.FINAL_BOSS;
    }

    @Override
    public boolean isCorrect(Task task, JsonNode correctAnswer, Map<String, Object> answer) {
        Object submitted = answer.get("culprit");
        if (submitted == null || correctAnswer.path("culprit").isMissingNode()) {
            return false;
        }
        return correctAnswer.path("culprit").asText().equalsIgnoreCase(String.valueOf(submitted));
    }
}
