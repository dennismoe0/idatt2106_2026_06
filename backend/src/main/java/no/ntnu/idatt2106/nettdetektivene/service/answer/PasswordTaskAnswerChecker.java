package no.ntnu.idatt2106.nettdetektivene.service.answer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.ntnu.idatt2106.nettdetektivene.entity.Task;
import no.ntnu.idatt2106.nettdetektivene.entity.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
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
                return checkBuilderAnswer(content, correctAnswer, answer);
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

    private boolean checkBuilderAnswer(JsonNode content, JsonNode correctAnswer, Map<String, Object> answer) {
        Object submittedPassword = answer.get("password");
        if (submittedPassword == null) {
            log.warn("[PasswordTaskAnswerChecker] PASSWORD BUILDER answer missing 'password' key");
            return false;
        }

        String password = String.valueOf(submittedPassword);
        int maxLength = configuredLimit(content, correctAnswer, "maxLength");
        if (maxLength > 0 && password.length() > maxLength) {
            log.info("[PasswordTaskAnswerChecker] PASSWORD BUILDER rejected due to maxLength");
            return false;
        }

        int maxParts = configuredLimit(content, correctAnswer, "maxParts");
        if (maxParts > 0 && submittedPartCount(content, answer, password) > maxParts) {
            log.info("[PasswordTaskAnswerChecker] PASSWORD BUILDER rejected due to maxParts");
            return false;
        }

        if (containsPitfall(content.path("pitfalls"), password)) {
            log.info("[PasswordTaskAnswerChecker] PASSWORD BUILDER rejected due to configured pitfall");
            return false;
        }

        String strength = passwordStrengthEvaluator.evaluate(password);
        log.info("[PasswordTaskAnswerChecker] PASSWORD BUILDER submitted strength={}", strength);

        return "STRONG".equals(strength);
    }

    private int configuredLimit(JsonNode content, JsonNode correctAnswer, String fieldName) {
        int contentLimit = content.path(fieldName).asInt(0);
        int answerLimit = correctAnswer.path(fieldName).asInt(0);
        if (contentLimit > 0 && answerLimit > 0) {
            return Math.min(contentLimit, answerLimit);
        }
        return Math.max(contentLimit, answerLimit);
    }

    private int submittedPartCount(JsonNode content, Map<String, Object> answer, String password) {
        List<String> tokens = configuredTokens(content);
        Object submittedParts = answer.get("parts");
        if (submittedParts instanceof Iterable<?> parts) {
            return validatedSubmittedPartCount(parts, tokens, password);
        }
        if (submittedParts instanceof Object[] parts) {
            return validatedSubmittedPartCount(List.of(parts), tokens, password);
        }

        return inferredPartCount(tokens, password);
    }

    private int validatedSubmittedPartCount(Iterable<?> parts, List<String> tokens, String password) {
        if (tokens.isEmpty()) {
            return Integer.MAX_VALUE;
        }

        int count = 0;
        StringBuilder rebuiltPassword = new StringBuilder();
        for (Object part : parts) {
            String value = String.valueOf(part);
            if (!tokens.contains(value)) {
                return Integer.MAX_VALUE;
            }
            rebuiltPassword.append(value);
            count++;
        }

        return rebuiltPassword.toString().equals(password) ? count : Integer.MAX_VALUE;
    }

    private List<String> configuredTokens(JsonNode content) {
        List<String> tokens = new ArrayList<>();
        addTokens(tokens, content.path("words"));
        addTokens(tokens, content.path("symbols"));
        addTokens(tokens, content.path("numbers"));
        return tokens;
    }

    private int inferredPartCount(List<String> tokens, String password) {
        if (tokens.isEmpty()) {
            return Integer.MAX_VALUE;
        }

        int[] minParts = new int[password.length() + 1];
        for (int i = 1; i < minParts.length; i++) {
            minParts[i] = Integer.MAX_VALUE;
        }

        for (int i = 0; i < password.length(); i++) {
            if (minParts[i] == Integer.MAX_VALUE) {
                continue;
            }
            for (String token : tokens) {
                if (password.startsWith(token, i)) {
                    int next = i + token.length();
                    minParts[next] = Math.min(minParts[next], minParts[i] + 1);
                }
            }
        }

        return minParts[password.length()];
    }

    private void addTokens(List<String> tokens, JsonNode node) {
        if (!node.isArray()) {
            return;
        }
        for (JsonNode token : node) {
            String value = token.asText("");
            if (!value.isBlank()) {
                tokens.add(value);
            }
        }
    }

    private boolean containsPitfall(JsonNode pitfalls, String password) {
        if (!pitfalls.isArray()) {
            return false;
        }

        String normalizedPassword = password.toLowerCase();
        for (JsonNode pitfall : pitfalls) {
            String value = pitfall.asText("");
            if (!value.isBlank() && normalizedPassword.contains(value.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
