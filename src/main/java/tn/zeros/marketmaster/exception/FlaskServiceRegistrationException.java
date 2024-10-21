package tn.zeros.marketmaster.exception;

public class FlaskServiceRegistrationException extends RuntimeException {
    public FlaskServiceRegistrationException(String message) {
        super(message);
    }

    public FlaskServiceRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}