package com.example.docMind.parser;

import com.example.docMind.exception.DocumentParsingException;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

@Component
public class PdfParser implements DocumentParser {

    @Override
    public String extractText(MultipartFile file) {
        var handler = new BodyContentHandler(-1);
        var context = new ParseContext();
        var parser = new PDFParser();
        var metadata = new Metadata();

        try (InputStream inputStream = file.getInputStream()) {
            parser.parse(inputStream, handler, metadata, context);
            return handler.toString();
        } catch (IOException | SAXException | TikaException e) {
            throw new DocumentParsingException(
                    "Failed to parse PDF file. The file may be corrupted or password-protected: " + e.getMessage(),
                    e
            );
        }
    }
}