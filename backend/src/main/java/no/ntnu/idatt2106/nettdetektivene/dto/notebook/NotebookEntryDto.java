package no.ntnu.idatt2106.nettdetektivene.dto.notebook;

import java.time.LocalDateTime;

/**
 * Response representing a single entry in a student's notebook, which may be a stop reflection or a general note.
 */
public record NotebookEntryDto(
    Long id,
    Long stopId,
    String stopName,
    Integer stopOrder,
    String entryType,
    String content,
    LocalDateTime createdAt
) {}
