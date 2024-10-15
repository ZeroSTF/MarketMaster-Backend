package tn.zeros.marketmaster.exception;

public class InsufficientHoldingsException extends RuntimeException{
    public InsufficientHoldingsException(String message) {
        super(message);
    }
}
