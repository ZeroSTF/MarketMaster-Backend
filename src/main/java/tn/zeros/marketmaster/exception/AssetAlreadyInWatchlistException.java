package tn.zeros.marketmaster.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class AssetAlreadyInWatchlistException extends RuntimeException {
    public AssetAlreadyInWatchlistException(String message) {
        super(message);
    }
}
