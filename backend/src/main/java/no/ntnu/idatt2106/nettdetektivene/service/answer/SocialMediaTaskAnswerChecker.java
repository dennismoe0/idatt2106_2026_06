package no.ntnu.idatt2106.nettdetektivene.service.answer;

import com.fasterxml.jackson.databind.JsonNode;
import no.ntnu.idatt2106.nettdetektivene.entity.Task;
import no.ntnu.idatt2106.nettdetektivene.entity.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;

/**
 * Evaluates answers for SOCIAL_MEDIA tasks, supporting single-action, accepted-actions, single-selection, and accepted-selection answer shapes.
 */
@Component
public class SocialMediaTaskAnswerChecker implements TaskAnswerChecker {

    private static final Logger log = LoggerFactory.getLogger(SocialMediaTaskAnswerChecker.class);

    /** {@inheritDoc} */
    @Override
    public TaskType supportedTaskType() {
        return TaskType.SOCIAL_MEDIA;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCorrect(Task task, JsonNode correctAnswer, Map<String, Object> answer) {
        if (!correctAnswer.path("action").isMissingNode()) {
            Object submitted = answer.get("action");
            if (submitted == null) {
                return false;
            }
            return correctAnswer.path("action").asText().equalsIgnoreCase(String.valueOf(submitted));
        }

        if (correctAnswer.path("acceptedActions").isArray()) {
            Object submitted = answer.get("action");
            if (submitted == null) {
                return false;
            }
            return matchesAny(correctAnswer.path("acceptedActions"), submitted);
        }

        if (!correctAnswer.path("selected").isMissingNode()) {
            Object submitted = answer.get("selected");
            if (submitted == null) {
                return false;
            }
            return correctAnswer.path("selected").asText().equalsIgnoreCase(String.valueOf(submitted));
        }

        if (correctAnswer.path("acceptedSelected").isArray()) {
            Object submitted = answer.get("selected");
            if (submitted == null) {
                return false;
            }
            return matchesAny(correctAnswer.path("acceptedSelected"), submitted);
        }

        log.warn("[SocialMediaTaskAnswerChecker] SOCIAL_MEDIA correctAnswer has no supported answer key");
        return false;
    }

    private boolean matchesAny(JsonNode acceptedValues, Object submitted) {
        Iterator<JsonNode> iterator = acceptedValues.elements();
        while (iterator.hasNext()) {
            if (iterator.next().asText().equalsIgnoreCase(String.valueOf(submitted))) {
                return true;
            }
        }
        return false;
    }
}
