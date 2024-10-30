package tn.zeros.marketmaster.exception;


public class AssetFetchException extends RuntimeException {
    public AssetFetchException(String message) {
        super(message);
    }

    public AssetFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}