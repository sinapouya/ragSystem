package com.example.docMind.parser;

import com.example.docMind.exception.DocumentParsingException;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class TextParser implements DocumentParser {

    @Override
    public String extractText(MultipartFile file) {
        // Simply read as UTF-8 string. IOException bubbles up to GlobalExceptionHandler.
        try {
            return StreamUtils.copyToString(file.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DocumentParsingException("Failed to read file: " + e.getMessage(), e);
        }
    }
}
