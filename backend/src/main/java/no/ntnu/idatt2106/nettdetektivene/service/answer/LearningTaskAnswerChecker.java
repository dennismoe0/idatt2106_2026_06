package no.ntnu.idatt2106.nettdetektivene.service.answer;

import com.fasterxml.jackson.databind.JsonNode;
import no.ntnu.idatt2106.nettdetektivene.entity.Task;
import no.ntnu.idatt2106.nettdetektivene.entity.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Learning tasks are fully validated in the frontend (must answer all quiz questions
 * correctly before submission is allowed). Backend always marks them as correct.
 */
@Component
public class LearningTaskAnswerChecker implements TaskAnswerChecker {

    private static final Logger log = LoggerFactory.getLogger(LearningTaskAnswerChecker.class);

    @Override
    public TaskType supportedTaskType() {
        return TaskType.LEARN;
    }

    @Override
    public boolean isCorrect(Task task, JsonNode correctAnswer, Map<String, Object> answer) {
        log.info("LEARN task {} completed by student — marking correct", task.getId());
        return true;
    }
}
