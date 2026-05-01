package no.ntnu.idatt2106.nettdetektivene.dto.notebook;

/**
 * Request payload for adding a free-form general note to a student's notebook (not tied to a stop).
 */
public record GeneralNoteRequest(String content) {}
