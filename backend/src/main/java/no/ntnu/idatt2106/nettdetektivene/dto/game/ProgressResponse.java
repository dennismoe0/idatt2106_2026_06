package no.ntnu.idatt2106.nettdetektivene.dto.game;

import java.util.List;

/**
 * Response summarising a student's overall game progress, including the status of every stop.
 */
public record ProgressResponse(
    int stopsCompleted,
    int totalStops,
    List<StopResponse> stops
) {}
