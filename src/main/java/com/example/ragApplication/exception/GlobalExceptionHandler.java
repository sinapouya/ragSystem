package com.example.ragApplication.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;
import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 1. Unsupported file format -> 415
    @ExceptionHandler(UnsupportedFileFormatException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedFileFormat(
            UnsupportedFileFormatException ex, WebRequest request) {

        var error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                "Unsupported Media Type",
                ex.getMessage(),
                extractPath(request)
        );
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(error);
    }

    // 2. Document parsing error (corrupted) -> 400
    @ExceptionHandler(DocumentParsingException.class)
    public ResponseEntity<ErrorResponse> handleDocumentParsing(
            DocumentParsingException ex, WebRequest request) {

        var error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Document Parsing Error",
                ex.getMessage(),
                extractPath(request)
        );
        return ResponseEntity.badRequest().body(error);
    }

    // 3. IO errors -> 500
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleIOException(
            IOException ex, WebRequest request) {

        var error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "File Read Error",
                "Could not read the uploaded file: " + ex.getMessage(),
                extractPath(request)
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // 4. File size limit exceeded -> 413
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxSizeException(
            MaxUploadSizeExceededException ex, WebRequest request) {

        var error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.PAYLOAD_TOO_LARGE.value(),
                "File Too Large",
                "The uploaded file exceeds the maximum allowed size (10MB).",
                extractPath(request)
        );
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(error);
    }

    // 5. Catch-all for unexpected errors -> 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {

        var error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred: " + ex.getMessage(),
                extractPath(request)
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // Helper: extract path from WebRequest
    private String extractPath(WebRequest request) {
        var description = request.getDescription(false);
        return description != null && description.startsWith("uri=")
                ? description.substring(4)
                : description;
    }
}