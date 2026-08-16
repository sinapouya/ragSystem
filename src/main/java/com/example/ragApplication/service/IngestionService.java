package com.example.ragApplication.service;

import com.example.ragApplication.controller.IngestionController;
import com.example.ragApplication.parser.ParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class IngestionService {
    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final VectorStore vectorStore;
    private final TokenTextSplitter splitter;
    private final ParserFactory parserFactory;

    public IngestionService(VectorStore vectorStore, ParserFactory parserFactory) {
        this.vectorStore = vectorStore;
        this.parserFactory = parserFactory;
        this.splitter = TokenTextSplitter.builder().
                withChunkSize(500)
                .withMinChunkSizeChars(100)
                .withMaxNumChunks(100)
                .build();
    }
    public int ingestText(String text, String sourceName) {
        Document doc = new Document(text);
        doc.getMetadata().put("source", sourceName);
        doc.getMetadata().put("ingested_at", java.time.Instant.now().toString());

        List<Document> chunks = splitter.apply(List.of(doc));
        vectorStore.add(chunks);

        return chunks.size();
    }
    public int ingestDocument(MultipartFile file, String sourceName) {
        log.info("Ingesting uploaded file: {}", file.getOriginalFilename());

        // 1. Get the appropriate parser from the factory
        var parser = parserFactory.getParser(file);

        // 2. Extract text as a String (throws unchecked exception on failure)
        String fullText = parser.extractText(file);

        // 3. Build a single Spring AI Document from the extracted text
        Document doc = new Document(
                fullText,
                Map.of(
                        "source", sourceName,
                        "filename", file.getOriginalFilename(),
                        "ingested_at", Instant.now().toString()
                )
        );
        // 4. Delegate to common storage logic
        return storeDocuments(List.of(doc));

    }
    public int ingestPdf(String filePath, String sourceName) {

        TikaDocumentReader reader = new TikaDocumentReader(
                new FileSystemResource(filePath)
        );
        List<Document> pages = reader.get();

        pages.forEach(page -> {
            page.getMetadata().put("source", sourceName);
            page.getMetadata().put("type", "pdf");
            page.getMetadata().put("ingested_at", java.time.Instant.now().toString());
        });

        List<Document> chunks = splitter.apply(pages);

        vectorStore.add(chunks);

        return chunks.size();
    }
    /**
     * Common logic: split documents into chunks and store them in the vector store.
     *
     * @param documents list of raw documents
     * @return number of chunks stored
     */
    private int storeDocuments(List<Document> documents) {
        // Apply splitting (chunking)
        List<Document> chunks = splitter.apply(documents);

        // Store in pgvector
        vectorStore.add(chunks);

        log.info("Stored {} chunks in vector store.", chunks.size());
        return chunks.size();
    }

}
