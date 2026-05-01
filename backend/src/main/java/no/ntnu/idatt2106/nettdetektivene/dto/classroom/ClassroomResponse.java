package no.ntnu.idatt2106.nettdetektivene.dto.classroom;

import java.time.LocalDateTime;

/**
 * Response representing a classroom, including its join code and creation timestamp.
 */
public record ClassroomResponse(
    Long id,
    String name,
    String joinCode,
    String description,
    LocalDateTime createdAt
) {}
