package tn.zeros.marketmaster.exception;

import java.util.List;

public class PortfolioValidationException extends RuntimeException {
    private final List<String> errors;

    public PortfolioValidationException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
