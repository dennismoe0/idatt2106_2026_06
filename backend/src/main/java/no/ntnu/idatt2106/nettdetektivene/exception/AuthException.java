package no.ntnu.idatt2106.nettdetektivene.exception;

/**
 * Thrown when an authentication or authorisation check fails — for example,
 * when credentials are invalid or when an operation is attempted by a user who
 * lacks the required role. Mapped to HTTP 401 by {@link GlobalExceptionHandler}.
 */
public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}
