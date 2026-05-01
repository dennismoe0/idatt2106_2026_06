package no.ntnu.idatt2106.nettdetektivene.dto.classroom;

/**
 * Response carrying a student's current enrollment status and music-muted preference.
 */
public record StudentStatusResponse(String status, boolean musicMuted) {}
