package no.ntnu.idatt2106.nettdetektivene.dto.classroom;

import java.time.LocalDateTime;

public record ClassroomResponse(
    Long id,
    String name,
    String joinCode,
    String description,
    LocalDateTime createdAt
) {}
