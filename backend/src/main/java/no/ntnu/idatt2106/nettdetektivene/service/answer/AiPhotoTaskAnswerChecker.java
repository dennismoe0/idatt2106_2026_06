package no.ntnu.idatt2106.nettdetektivene.service.answer;

import com.fasterxml.jackson.databind.JsonNode;
import no.ntnu.idatt2106.nettdetektivene.entity.Task;
import no.ntnu.idatt2106.nettdetektivene.entity.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Evaluates answers for AI_PHOTO tasks, supporting both artifact-finding and image-classification subtypes.
 */
@Component
public class AiPhotoTaskAnswerChecker implements TaskAnswerChecker {

    private static final Logger log = LoggerFactory.getLogger(AiPhotoTaskAnswerChecker.class);

    /** {@inheritDoc} */
    @Override
    public TaskType supportedTaskType() {
        return TaskType.AI_PHOTO;
    }

    /**
     * {@inheritDoc}
     * <p>Dispatches to artifact-finding or image-classification evaluation based on the correct-answer shape.</p>
     */
    @Override
    public boolean isCorrect(Task task, JsonNode correctAnswer, Map<String, Object> answer) {
        if (!correctAnswer.path("foundArtifactIds").isMissingNode()) {
            return checkFindArtifacts(correctAnswer, answer);
        }
        return checkClassify(correctAnswer, answer);
    }

    private boolean checkFindArtifacts(JsonNode correctAnswer, Map<String, Object> answer) {
        Set<String> required = new HashSet<>();
        correctAnswer.path("foundArtifactIds").forEach(n -> required.add(n.asText()));

        Object submitted = answer.get("foundArtifactIds");
        Set<String> found = new HashSet<>();
        if (submitted instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                found.add(String.valueOf(item));
            }
        }

        boolean result = found.containsAll(required);
        log.info("[AiPhotoTaskAnswerChecker] FIND_ARTIFACTS — required: {} found: {} pass: {}",
            required, found, result);
        return result;
    }

    private boolean checkClassify(JsonNode correctAnswer, Map<String, Object> answer) {
        Iterator<Map.Entry<String, JsonNode>> fields = correctAnswer.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            Object submitted = answer.get(field.getKey());
            if (submitted == null) {
                log.warn("[AiPhotoTaskAnswerChecker] AI_PHOTO answer missing key: {}", field.getKey());
                return false;
            }
            if (!field.getValue().asText().equalsIgnoreCase(String.valueOf(submitted))) {
                return false;
            }
        }
        return true;
    }
}
