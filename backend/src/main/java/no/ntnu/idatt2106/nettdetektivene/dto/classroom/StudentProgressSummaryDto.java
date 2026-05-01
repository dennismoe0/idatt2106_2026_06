package no.ntnu.idatt2106.nettdetektivene.dto.classroom;

/**
 * Response summarising a student's overall game progress as seen by a teacher in the classroom dashboard.
 */
public record StudentProgressSummaryDto(
    Long studentId,
    String displayName,
    int completedTasks,
    String currentStopName,
    int currentStopOrder,
    String lastCompletedStopName
) {}
