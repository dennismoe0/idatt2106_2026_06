package no.ntnu.idatt2106.nettdetektivene.service.answer;

import com.fasterxml.jackson.databind.JsonNode;
import no.ntnu.idatt2106.nettdetektivene.entity.Task;
import no.ntnu.idatt2106.nettdetektivene.entity.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SocialMediaTaskAnswerChecker implements TaskAnswerChecker {

    private static final Logger log = LoggerFactory.getLogger(SocialMediaTaskAnswerChecker.class);

    @Override
    public TaskType supportedTaskType() {
        return TaskType.SOCIAL_MEDIA;
    }

    @Override
    public boolean isCorrect(Task task, JsonNode correctAnswer, Map<String, Object> answer) {
        if (!correctAnswer.path("action").isMissingNode()) {
            Object submitted = answer.get("action");
            if (submitted == null) {
                return false;
            }
            return correctAnswer.path("action").asText().equalsIgnoreCase(String.valueOf(submitted));
        }

        if (!correctAnswer.path("selected").isMissingNode()) {
            Object submitted = answer.get("selected");
            if (submitted == null) {
                return false;
            }
            return correctAnswer.path("selected").asText().equalsIgnoreCase(String.valueOf(submitted));
        }

        log.warn("[SocialMediaTaskAnswerChecker] SOCIAL_MEDIA correctAnswer has neither 'action' nor 'selected' key");
        return false;
    }
}
