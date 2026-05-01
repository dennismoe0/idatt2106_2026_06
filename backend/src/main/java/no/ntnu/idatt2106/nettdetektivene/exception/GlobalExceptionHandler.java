package no.ntnu.idatt2106.nettdetektivene.exception;

import no.ntnu.idatt2106.nettdetektivene.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import java.util.stream.Collectors;

/**
 * Centralised exception handler that translates application exceptions into
 * consistent {@code ErrorResponse} JSON bodies. All controllers rely on this
 * class instead of handling exceptions individually.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles authentication and authorisation failures.
     * Returns HTTP 401 with the exception message.
     */
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuth(AuthException ex) {
        log.warn("Auth error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(ex.getMessage()));
    }

    /**
     * Handles requests for entities that do not exist.
     * Returns HTTP 404 with the exception message.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

    /**
     * Handles bean-validation failures on request bodies annotated with {@code @Valid}.
     * Collects all field errors into a single comma-separated message and returns HTTP 400.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", message);
        return ResponseEntity.badRequest().body(new ErrorResponse(message));
    }

    /**
     * Handles missing required query or path parameters.
     * Returns HTTP 400 with the name of the missing parameter.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        log.warn("Missing required parameter: {}", ex.getParameterName());
        return ResponseEntity.badRequest().body(new ErrorResponse("Missing required parameter: " + ex.getParameterName()));
    }

    /**
     * Handles malformed or missing request bodies (e.g. invalid JSON syntax).
     * Returns HTTP 400 with a generic "Invalid request body" message to avoid
     * leaking internal details.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableMessage(HttpMessageNotReadableException ex) {
        log.warn("Request body could not be read: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse("Invalid request body"));
    }

    /**
     * Handles Spring Security access-denied events (e.g. method-level {@code @PreAuthorize} failures).
     * Returns HTTP 403.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse("Access denied"));
    }

    /**
     * Handles {@link org.springframework.web.server.ResponseStatusException} instances
     * thrown directly from service or controller code with an explicit HTTP status.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        log.warn("Request failed: {}", ex.getReason());
        return ResponseEntity.status(ex.getStatusCode()).body(new ErrorResponse(ex.getReason()));
    }

    /**
     * Catch-all handler for any unhandled exception. Logs the full stack trace
     * and returns HTTP 500 with a generic error message.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("An unexpected error occurred"));
    }
}
