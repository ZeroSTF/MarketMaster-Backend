package tn.zeros.marketmaster.exception;

public class UserProgressAlreadyExistsException extends RuntimeException {
    public UserProgressAlreadyExistsException(String message) {
        super(message);
    }
}