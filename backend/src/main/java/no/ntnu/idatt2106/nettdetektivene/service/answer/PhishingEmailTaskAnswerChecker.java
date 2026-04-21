package no.ntnu.idatt2106.nettdetektivene.service.answer;

import com.fasterxml.jackson.databind.JsonNode;
import no.ntnu.idatt2106.nettdetektivene.entity.Task;
import no.ntnu.idatt2106.nettdetektivene.entity.TaskType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PhishingEmailTaskAnswerChecker implements TaskAnswerChecker {

    @Override
    public TaskType supportedTaskType() {
        return TaskType.PHISHING_EMAIL;
    }

    @Override
    public boolean isCorrect(Task task, JsonNode correctAnswer, Map<String, Object> answer) {
        Object submittedAction = answer.get("action");
        if (submittedAction == null || correctAnswer.path("action").isMissingNode()) {
            return false;
        }
        return correctAnswer.path("action").asText().equalsIgnoreCase(String.valueOf(submittedAction));
    }
}
