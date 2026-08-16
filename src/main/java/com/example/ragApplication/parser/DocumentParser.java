package com.example.ragApplication.parser;

import org.springframework.web.multipart.MultipartFile;

public interface DocumentParser {

    String extractText(MultipartFile file);
    // throws exceptions handled by GlobalExceptionHandler
}