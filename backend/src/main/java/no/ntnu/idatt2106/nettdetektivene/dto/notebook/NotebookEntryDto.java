package no.ntnu.idatt2106.nettdetektivene.dto.notebook;

import java.time.LocalDateTime;

public record NotebookEntryDto(
    Long id,
    Long stopId,
    String stopName,
    Integer stopOrder,
    String entryType,
    String content,
    LocalDateTime createdAt
) {}
