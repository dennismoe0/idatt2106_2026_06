package no.ntnu.idatt2106.nettdetektivene.dto.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record NotificationDto(
    Long id,
    String type,
    String message,
    Long referenceId,
    Long classroomId,
    Long studentId,
    @JsonProperty("isRead")
    boolean isRead,
    LocalDateTime createdAt
) {
}
