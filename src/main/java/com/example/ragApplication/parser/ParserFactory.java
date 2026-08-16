package com.example.ragApplication.parser;

import com.example.ragApplication.exception.UnsupportedFileFormatException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Component
public class ParserFactory {

    private final Map<String, DocumentParser> parserMap;

    public ParserFactory(
            PdfParser pdfParser,
            DocxParser docxParser,
            EpubParser epubParser,
            TextParser textParser
    ) {
        // Build a map: extension → parser instance
        this.parserMap = Map.ofEntries(
                // PDF
                Map.entry("pdf", pdfParser),

                // DOCX
                Map.entry("docx", docxParser),

                // EPUB
                Map.entry("epub", epubParser),

                // Plain text formats (all use TxtParser)
                Map.entry("txt", textParser),
                Map.entry("log", textParser),
                Map.entry("csv", textParser),

                // Markdown formats (both use MarkdownParser)
                Map.entry("md", textParser),
                Map.entry("markdown", textParser)
        );
    }
    public DocumentParser getParser(MultipartFile file) {
        var originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.isBlank()) {
            throw new UnsupportedFileFormatException(
                    "File has no name. Cannot determine file type."
            );
        }

        // Extract extension using Java's new String methods
        var extension = "";
        var dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < originalFilename.length() - 1) {
            extension = originalFilename.substring(dotIndex + 1).toLowerCase();
        }

        var parser = this.parserMap.get(extension);

        // Modern Switch Expression with yield (Java 14+)
        return switch (extension) {
            case "pdf"   -> new PdfParser();
            case "docx"  -> new DocxParser();
            case "epub"  -> new EpubParser();
            case "txt", "log", "csv","md","markdown" -> new TextParser();
            default -> throw new UnsupportedFileFormatException(
                    "Unsupported file format: '" + extension + "'. " +
                            "Supported formats: pdf, docx, epub, txt, md"
            );
        };
    }
}
