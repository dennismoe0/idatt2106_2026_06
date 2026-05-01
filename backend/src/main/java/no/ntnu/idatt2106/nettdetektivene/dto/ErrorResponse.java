package no.ntnu.idatt2106.nettdetektivene.dto;

/**
 * Generic error response returned by the GlobalExceptionHandler for all error paths.
 */
public record ErrorResponse(String error) {}
