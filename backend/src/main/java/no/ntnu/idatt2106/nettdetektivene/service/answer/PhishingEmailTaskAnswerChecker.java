package no.ntnu.idatt2106.nettdetektivene.service.answer;

import com.fasterxml.jackson.databind.JsonNode;
import no.ntnu.idatt2106.nettdetektivene.entity.Task;
import no.ntnu.idatt2106.nettdetektivene.entity.TaskType;
import no.ntnu.idatt2106.nettdetektivene.service.PhishingAnswerChecker;
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
        return PhishingAnswerChecker.check(correctAnswer, answer);
    }
}
