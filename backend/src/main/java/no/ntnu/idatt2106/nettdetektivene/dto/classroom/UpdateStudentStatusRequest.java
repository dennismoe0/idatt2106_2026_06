package no.ntnu.idatt2106.nettdetektivene.dto.classroom;

import jakarta.validation.constraints.NotNull;
import no.ntnu.idatt2106.nettdetektivene.model.ClassroomStudentStatus;

public record UpdateStudentStatusRequest(
    @NotNull ClassroomStudentStatus status
) {}
