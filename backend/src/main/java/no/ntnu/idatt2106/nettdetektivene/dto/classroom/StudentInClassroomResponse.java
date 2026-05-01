package no.ntnu.idatt2106.nettdetektivene.dto.classroom;

/**
 * Response representing a student's membership details within a specific classroom.
 */
public record StudentInClassroomResponse(
    Long userId,
    Long classroomId,
    String displayName,
    String username,
    String status
) {}
