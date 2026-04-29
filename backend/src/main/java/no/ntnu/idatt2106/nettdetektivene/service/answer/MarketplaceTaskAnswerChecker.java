package no.ntnu.idatt2106.nettdetektivene.service.answer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.ntnu.idatt2106.nettdetektivene.entity.Task;
import no.ntnu.idatt2106.nettdetektivene.entity.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class MarketplaceTaskAnswerChecker implements TaskAnswerChecker {

    private static final Logger log = LoggerFactory.getLogger(MarketplaceTaskAnswerChecker.class);

    private final ObjectMapper objectMapper;

    public MarketplaceTaskAnswerChecker(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public TaskType supportedTaskType() {
        return TaskType.MARKETPLACE;
    }

    @Override
    public boolean isCorrect(Task task, JsonNode correctAnswer, Map<String, Object> answer) {
        if (!correctAnswer.path("correctElementIds").isMissingNode()) {
            return checkClickSuspicious(task, correctAnswer, answer);
        }
        return checkLegacy(correctAnswer, answer);
    }

    private boolean checkClickSuspicious(Task task, JsonNode correctAnswer, Map<String, Object> answer) {
        Set<String> correct = new HashSet<>();
        correctAnswer.path("correctElementIds").forEach(n -> correct.add(n.asText()));

        // Temporary compatibility guard for already-seeded tasks in older environments
        // where correctElementIds may be empty in DB.
        // TODO(#309): remove once marketplace task data is backfilled in all environments.
        if (correct.isEmpty() && task != null && task.getContentJson() != null) {
            log.warn("[MarketplaceTaskAnswerChecker] correctElementIds empty — falling back to contentJson.elements");
            try {
                JsonNode content = objectMapper.readTree(task.getContentJson());
                content.path("elements").forEach(el -> {
                    if (el.path("isSuspicious").asBoolean(false)) {
                        correct.add(el.path("id").asText());
                    }
                });
            } catch (JsonProcessingException e) {
                log.error("[MarketplaceTaskAnswerChecker] failed to parse contentJson for fallback", e);
            }
        }

        Object submitted = answer.get("flaggedElementIds");
        Set<String> flagged = new HashSet<>();
        if (submitted instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                flagged.add(String.valueOf(item));
            }
        }

        boolean result = correct.equals(flagged);
        log.info("[MarketplaceTaskAnswerChecker] CLICK_SUSPICIOUS — correct: {} flagged: {} match: {}",
            correct, flagged, result);
        return result;
    }

    private boolean checkLegacy(JsonNode correctAnswer, Map<String, Object> answer) {
        Object submitted = answer.get("selected");
        if (submitted == null || correctAnswer.path("selected").isMissingNode()) {
            log.warn("[MarketplaceTaskAnswerChecker] MARKETPLACE answer missing 'selected' key");
            return false;
        }
        return correctAnswer.path("selected").asText().equalsIgnoreCase(String.valueOf(submitted));
    }
}
