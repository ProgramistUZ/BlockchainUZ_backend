package org.example.blockchainuz.exception;

/**
 * Exception thrown when there's an error communicating with the blockchain node
 */
public class CryptoNodeException extends RuntimeException {

    public CryptoNodeException(String message) {
        super(message);
    }

    public CryptoNodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
