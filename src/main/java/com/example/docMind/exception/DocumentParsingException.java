package com.example.docMind.exception;

/**
 * Thrown when a supported file cannot be parsed (e.g., corrupted).
 * Maps to HTTP 400 (Bad Request).
 */
public class DocumentParsingException extends RuntimeException {

    public DocumentParsingException(String message) {
        super(message);
    }

    public DocumentParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}