package no.ntnu.idatt2106.nettdetektivene.service.answer;

import com.fasterxml.jackson.databind.JsonNode;
import no.ntnu.idatt2106.nettdetektivene.entity.Task;
import no.ntnu.idatt2106.nettdetektivene.entity.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;

@Component
public class AiPhotoTaskAnswerChecker implements TaskAnswerChecker {

    private static final Logger log = LoggerFactory.getLogger(AiPhotoTaskAnswerChecker.class);

    @Override
    public TaskType supportedTaskType() {
        return TaskType.AI_PHOTO;
    }

    @Override
    public boolean isCorrect(Task task, JsonNode correctAnswer, Map<String, Object> answer) {
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
