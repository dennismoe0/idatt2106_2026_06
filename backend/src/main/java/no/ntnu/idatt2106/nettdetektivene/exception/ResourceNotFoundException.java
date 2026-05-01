package no.ntnu.idatt2106.nettdetektivene.exception;

/**
 * Thrown when a requested entity cannot be found in the database — for example,
 * when a client requests a classroom or task by an id that does not exist.
 * Mapped to HTTP 404 by {@link GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
