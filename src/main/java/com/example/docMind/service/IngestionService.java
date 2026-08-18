package com.example.docMind.service;

import com.example.docMind.entity.Chapter;
import com.example.docMind.parser.ParserFactory;
import com.example.docMind.repository.ChapterRepository;
import com.example.docMind.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class IngestionService {
    private final VectorStore vectorStore;
    private final ParserFactory parserFactory;
    private final ChapterDetectionService chapterDetectionService;
    private final DocumentRepository documentRepository;
    private final ChapterRepository chapterRepository;
    private final MinioStorageService minioStorageService;

    private final TokenTextSplitter splitter = TokenTextSplitter.builder()
            .withChunkSize(500)
            .withMinChunkSizeChars(100)
            .withMaxNumChunks(100)
            .build();
    @Transactional
    public int ingestText(String text, String sourceName) {
        log.info(" Ingesting raw text from source: {}", sourceName);

        // Generate a random filename starting with "raw-text-"
        String fileName = generateRandomFilename();

        String minioPath = minioStorageService.uploadTextContent(text, fileName);

        return processDocumentContent(
                text,
                fileName,
                "txt",
                sourceName,
                minioPath
        );
    }
    @Transactional
    public int ingestDocument(MultipartFile file, String sourceName) {
        log.info("Ingesting uploaded file: {}", file.getOriginalFilename());

        String minioPath = minioStorageService.uploadFile(file);

        var parser = parserFactory.getParser(file);

        String fullText = parser.extractText(file);

        // Process with shared logic
        return processDocumentContent(
                fullText,
                file.getOriginalFilename(),
                getFileExtension(file.getOriginalFilename()),
                sourceName,
                minioPath
        );

    }
    private int processDocumentContent(
            String fullText,
            String fileName,
            String fileType,
            String sourceName,
            String minioPath) {

        // 1. Save Document entity
        com.example.docMind.entity.Document document = com.example.docMind.entity.Document.builder()
                .fileName(fileName)
                .fileType(fileType)
                .fullText(fullText)
                .source(sourceName)
                .minioPath(minioPath)
                .build();

        com.example.docMind.entity.Document savedDoc = documentRepository.save(document);
        log.info("✅ Saved document ID: {}", savedDoc.getId());

        // 2. Detect and save chapters
        List<Chapter> chapters = chapterDetectionService.detectChapters(fullText);
        for (Chapter chapter : chapters) {
            chapter.setDocument(savedDoc);
        }
        chapterRepository.saveAll(chapters);
        log.info("📚 Detected {} chapters", chapters.size());

        // 3. RAG Pipeline – chunk, embed, store
        int chunkCount = processForRag(fullText, Map.of(
                "source", sourceName,
                "filename", fileName,
                "documentId", savedDoc.getId().toString(),
                "minioPath", minioPath != null ? minioPath : "N/A"
        ));

        log.info("✅ Ingested {} chunks from '{}'", chunkCount, fileName);
        return chunkCount;
    }
    /**
     * Common RAG logic: split, embed, and store in vector DB.
     */
    private int processForRag(String text, Map<String, Object> metadata) {
        Document doc = new Document(text, metadata);
        List<Document> chunks = splitter.apply(List.of(doc));
        vectorStore.add(chunks);
        return chunks.size();
    }
    private int storeDocuments(List<Document> documents) {
        List<Document> chunks = splitter.apply(documents);
        vectorStore.add(chunks);
        log.info("Stored {} chunks in vector store.", chunks.size());
        return chunks.size();
    }
    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return (dot > 0) ? filename.substring(dot + 1).toLowerCase() : "";
    }
    private String generateRandomFilename() {
        return "raw-text-" + UUID.randomUUID() + ".txt";
    }
}
