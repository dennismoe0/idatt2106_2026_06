package no.ntnu.idatt2106.nettdetektivene.dto.school;

import no.ntnu.idatt2106.nettdetektivene.dto.classroom.LeaderboardEntryDto;
import java.util.List;

public record SchoolClassroomSummary(
    Long classroomId,
    String name,
    String description,
    int studentCount,
    List<LeaderboardEntryDto> top5
) {}
