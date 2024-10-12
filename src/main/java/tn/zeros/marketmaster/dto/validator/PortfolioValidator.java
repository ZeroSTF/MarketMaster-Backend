package tn.zeros.marketmaster.dto.validator;

import tn.zeros.marketmaster.dto.PortfolioDTO;
import tn.zeros.marketmaster.exception.PortfolioValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PortfolioValidator {

    public void validate(PortfolioDTO portfolioDTO) throws PortfolioValidationException {
        List<String> errors = new ArrayList<>();

        if (portfolioDTO.getId() != null && portfolioDTO.getId() < 0) {
            errors.add("Portfolio ID must be null for new portfolios or a positive value for existing ones.");
        }

        if (portfolioDTO.getUserId() == null) {
            errors.add("User ID cannot be null.");
        }

        if (portfolioDTO.getHoldings() == null || portfolioDTO.getHoldings().isEmpty()) {
            errors.add("Portfolio must contain at least one holding.");
        } else {
            portfolioDTO.getHoldings().forEach(holdingDTO -> {
                if (holdingDTO.getQuantity() == null || holdingDTO.getQuantity() <= 0) {
                    errors.add("Each holding must have a valid quantity greater than 0.");
                }
                if (holdingDTO.getAssetId() == null) {
                    errors.add("Each holding must have an associated asset ID.");
                }
            });
        }

        if (portfolioDTO.getTotalValue() != null) {
            for (Map.Entry<?, Double> entry : portfolioDTO.getTotalValue().entrySet()) {
                if (entry.getValue() == null || entry.getValue() < 0) {
                    errors.add("Portfolio total value must not be null or negative.");
                }
            }
        }

        if (portfolioDTO.getCash() < 0) {
            errors.add("Cash balance cannot be negative.");
        }

        if (!errors.isEmpty()) {
            throw new PortfolioValidationException("ERROR: ", errors);
        }
    }
}
