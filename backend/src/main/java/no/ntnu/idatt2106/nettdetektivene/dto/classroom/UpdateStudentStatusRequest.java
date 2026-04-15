package no.ntnu.idatt2106.nettdetektivene.dto.classroom;

import jakarta.validation.constraints.NotNull;
import no.ntnu.idatt2106.nettdetektivene.entity.ClassroomStudent;

public record UpdateStudentStatusRequest(
    @NotNull ClassroomStudent.Status status
) {}
