package no.ntnu.idatt2106.nettdetektivene.dto.classroom;

public record StudentInClassroomResponse(
    Long userId,
    Long classroomId,
    String displayName,
    String status
) {}
