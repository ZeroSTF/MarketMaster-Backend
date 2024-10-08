package tn.zeros.marketmaster.service;

import tn.zeros.marketmaster.dto.PortfolioDTO;
import tn.zeros.marketmaster.exception.PortfolioValidationException;

import java.util.ArrayList;
import java.util.List;

public class PortfolioValidator {
    public void validate(PortfolioDTO portfolioDTO) throws PortfolioValidationException {
        List<String> errors = new ArrayList<>();

        if (portfolioDTO.getUserId() == null) {
            errors.add("User ID is required");
        }

        if (portfolioDTO.getCash() < 0) {
            errors.add("Cash amount cannot be negative");
        }

        if (portfolioDTO.getAnnualReturn() < -100) {
            errors.add("Annual return cannot be less than -100%");
        }

        if (portfolioDTO.getCurrentRank() != null && portfolioDTO.getCurrentRank() < 1) {
            errors.add("Current rank must be a positive number");
        }

        if (!errors.isEmpty()) {
            throw new PortfolioValidationException("Portfolio validation failed", errors);
        }
    }
}
