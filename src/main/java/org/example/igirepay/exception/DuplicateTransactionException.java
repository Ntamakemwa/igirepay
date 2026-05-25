package org.example.igirepay.exception;

public class DuplicateTransactionException extends Exception {
    public DuplicateTransactionException(String message) {
        super(message);
    }
}
