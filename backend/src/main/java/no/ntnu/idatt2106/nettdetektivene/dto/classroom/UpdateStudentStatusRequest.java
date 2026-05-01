package no.ntnu.idatt2106.nettdetektivene.dto.classroom;

import jakarta.validation.constraints.NotNull;
import no.ntnu.idatt2106.nettdetektivene.model.ClassroomStudentStatus;

/**
 * Request payload for a teacher to approve or kick a student from a classroom.
 */
public record UpdateStudentStatusRequest(
    @NotNull ClassroomStudentStatus status
) {}
