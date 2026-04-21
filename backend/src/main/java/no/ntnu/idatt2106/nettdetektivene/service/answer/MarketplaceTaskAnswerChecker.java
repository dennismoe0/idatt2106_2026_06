package no.ntnu.idatt2106.nettdetektivene.service.answer;

import com.fasterxml.jackson.databind.JsonNode;
import no.ntnu.idatt2106.nettdetektivene.entity.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MarketplaceTaskAnswerChecker implements TaskAnswerChecker {

    private static final Logger log = LoggerFactory.getLogger(MarketplaceTaskAnswerChecker.class);

    @Override
    public TaskType supportedTaskType() {
        return TaskType.MARKETPLACE;
    }

    @Override
    public boolean isCorrect(JsonNode correctAnswer, Map<String, Object> answer) {
        Object submitted = answer.get("selected");
        if (submitted == null || correctAnswer.path("selected").isMissingNode()) {
            log.warn("[MarketplaceTaskAnswerChecker] MARKETPLACE answer missing 'selected' key");
            return false;
        }
        return correctAnswer.path("selected").asText().equalsIgnoreCase(String.valueOf(submitted));
    }
}
