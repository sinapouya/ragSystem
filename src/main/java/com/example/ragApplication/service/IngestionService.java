package com.example.ragApplication.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IngestionService {
    private final VectorStore vectorStore;
    private final TokenTextSplitter splitter;

    public IngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
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
    public int ingestDocument(String filePath, String sourceName) {
        TikaDocumentReader reader = new TikaDocumentReader(
                new FileSystemResource(filePath)
        );
        List<Document> docs = reader.get();

        docs.forEach(doc -> {
            doc.getMetadata().put("source", sourceName);
            doc.getMetadata().put("ingested_at", java.time.Instant.now().toString());
        });

        List<Document> chunks = splitter.apply(docs);
        vectorStore.add(chunks);

        return chunks.size();
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


}
