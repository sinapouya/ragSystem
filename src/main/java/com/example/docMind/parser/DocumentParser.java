package com.example.docMind.parser;

import org.springframework.web.multipart.MultipartFile;

public interface DocumentParser {

    String extractText(MultipartFile file);
    // throws exceptions handled by GlobalExceptionHandler
}