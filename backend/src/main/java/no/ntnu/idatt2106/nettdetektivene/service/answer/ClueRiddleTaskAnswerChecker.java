package no.ntnu.idatt2106.nettdetektivene.service.answer;

import com.fasterxml.jackson.databind.JsonNode;
import no.ntnu.idatt2106.nettdetektivene.entity.Task;
import no.ntnu.idatt2106.nettdetektivene.entity.TaskType;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Evaluates answers for CLUE_RIDDLE tasks, supporting both single and multi-select correct answers.
 */
@Component
public class ClueRiddleTaskAnswerChecker implements TaskAnswerChecker {

    /** {@inheritDoc} */
    @Override
    public TaskType supportedTaskType() {
        return TaskType.CLUE_RIDDLE;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCorrect(Task task, JsonNode correctAnswer, Map<String, Object> answer) {
        Object submitted = answer.get("selected");
        if (submitted == null || correctAnswer.path("selected").isMissingNode()) {
            return false;
        }
        JsonNode selected = correctAnswer.path("selected");
        if (selected.isArray()) {
            return selectedSet(selected).equals(submittedSet(submitted));
        }
        return correctAnswer.path("selected").asText().equals(String.valueOf(submitted));
    }

    private Set<String> selectedSet(JsonNode selected) {
        Set<String> ids = new HashSet<>();
        selected.forEach(id -> ids.add(id.asText()));
        return ids;
    }

    private Set<String> submittedSet(Object submitted) {
        if (submitted instanceof List<?> list) {
            Set<String> ids = new HashSet<>();
            list.forEach(id -> ids.add(String.valueOf(id)));
            return ids;
        }
        return Set.of(String.valueOf(submitted));
    }
}
