package no.ntnu.idatt2106.nettdetektivene.dto.classroom;

public record StudentProgressSummaryDto(
    Long studentId,
    String displayName,
    int completedTasks,
    String currentStopName,
    int currentStopOrder,
    String lastCompletedStopName
) {}
