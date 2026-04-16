package no.ntnu.idatt2106.nettdetektivene.dto.game;

import java.util.List;

public record ProgressResponse(
    int stopsCompleted,
    int totalStops,
    List<StopResponse> stops
) {}
