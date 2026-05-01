package no.ntnu.idatt2106.nettdetektivene.service.answer;

import com.fasterxml.jackson.databind.JsonNode;
import no.ntnu.idatt2106.nettdetektivene.entity.Task;
import no.ntnu.idatt2106.nettdetektivene.entity.TaskType;

import java.util.Map;

/**
 * Strategy interface for evaluating a student's answer to a specific task type.
 */
public interface TaskAnswerChecker {

    /**
     * Returns the task type this checker handles.
     *
     * @return the supported {@link TaskType}
     */
    TaskType supportedTaskType();

    /**
     * Evaluates whether the submitted answer is correct.
     *
     * @param task          the task being answered
     * @param correctAnswer the authoritative correct-answer node from the database
     * @param answer        the student's submitted answer as a key-value map
     * @return {@code true} if the answer is correct
     */
    boolean isCorrect(Task task, JsonNode correctAnswer, Map<String, Object> answer);
}
