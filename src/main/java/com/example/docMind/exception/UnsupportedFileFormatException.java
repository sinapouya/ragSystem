package com.example.docMind.exception;

/**
 * Thrown when a file format is not supported by the system.
 * Maps to HTTP 415 (Unsupported Media Type).
 */
public class UnsupportedFileFormatException extends RuntimeException {

    public UnsupportedFileFormatException(String message) {
        super(message);
    }

    public UnsupportedFileFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
