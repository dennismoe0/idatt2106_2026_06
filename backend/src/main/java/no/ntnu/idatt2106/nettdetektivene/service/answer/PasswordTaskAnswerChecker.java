package no.ntnu.idatt2106.nettdetektivene.service.answer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.ntnu.idatt2106.nettdetektivene.entity.Task;
import no.ntnu.idatt2106.nettdetektivene.entity.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PasswordTaskAnswerChecker implements TaskAnswerChecker {

    private static final Logger log = LoggerFactory.getLogger(PasswordTaskAnswerChecker.class);

    private final ObjectMapper objectMapper;
    private final PasswordStrengthEvaluator passwordStrengthEvaluator;

    public PasswordTaskAnswerChecker(
        ObjectMapper objectMapper,
        PasswordStrengthEvaluator passwordStrengthEvaluator
    ) {
        this.objectMapper = objectMapper;
        this.passwordStrengthEvaluator = passwordStrengthEvaluator;
    }

    @Override
    public TaskType supportedTaskType() {
        return TaskType.PASSWORD;
    }

    @Override
    public boolean isCorrect(Task task, JsonNode correctAnswer, Map<String, Object> answer) {
        try {
            JsonNode content = objectMapper.readTree(task.getContentJson());
            String subtype = content.path("type").asText("CHOICE");

            if ("BUILDER".equals(subtype)) {
                return checkBuilderAnswer(correctAnswer, answer);
            }

            Object submitted = answer.get("selected");
            if (submitted == null || correctAnswer.path("selected").isMissingNode()) {
                return false;
            }
            return correctAnswer.path("selected").asText().equalsIgnoreCase(String.valueOf(submitted));
        } catch (JsonProcessingException exception) {
            log.error("[PasswordTaskAnswerChecker] Failed to parse PASSWORD contentJson taskId={}", task.getId(), exception);
            return false;
        }
    }

    private boolean checkBuilderAnswer(JsonNode correctAnswer, Map<String, Object> answer) {
        String requiredStrength = correctAnswer.path("minStrength").asText("STRONG");
        Object submittedPassword = answer.get("password");
        if (submittedPassword == null) {
            log.warn("[PasswordTaskAnswerChecker] PASSWORD BUILDER answer missing 'password' key");
            return false;
        }

        String strength = passwordStrengthEvaluator.evaluate(String.valueOf(submittedPassword));
        log.info(
            "[PasswordTaskAnswerChecker] PASSWORD BUILDER submitted strength={} required={}",
            strength,
            requiredStrength
        );

        int submitted = passwordStrengthEvaluator.strengthLevel(strength);
        int required = passwordStrengthEvaluator.strengthLevel(requiredStrength);
        return submitted >= required;
    }
}
