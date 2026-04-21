package no.ntnu.idatt2106.nettdetektivene.service.answer;

import com.fasterxml.jackson.databind.JsonNode;
import no.ntnu.idatt2106.nettdetektivene.entity.TaskType;

import java.util.Map;

public interface TaskAnswerChecker {

    TaskType supportedTaskType();

    boolean isCorrect(JsonNode correctAnswer, Map<String, Object> answer);
}
