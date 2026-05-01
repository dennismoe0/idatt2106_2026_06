package no.ntnu.idatt2106.nettdetektivene.dto.school;

/**
 * Response representing a school, including its join code so teachers can link classrooms to it.
 */
public record SchoolResponse(Long id, String name, String joinCode) {}
