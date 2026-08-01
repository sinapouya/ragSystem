package com.example.ragApplication.controller;

import com.example.ragApplication.enums.ResponseStatus;
import com.example.ragApplication.model.ApiResponse;
import com.example.ragApplication.model.IngestResponse;
import com.example.ragApplication.model.Ingest;
import com.example.ragApplication.service.IngestionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/ingest")
public class IngestionController {

    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/text")
    public ResponseEntity<ApiResponse<IngestResponse>> ingestText(@RequestBody @NotNull Ingest ingest) {
        String source = ingest.source()==null?"manual":ingest.source();
        int chunkCount = ingestionService.ingestText(ingest.text(), source);
        IngestResponse ingetIngestResponse = new IngestResponse(
                chunkCount,
                source
        );
        return ResponseEntity.ok(new ApiResponse<>(ingetIngestResponse));
    }
    @PostMapping(value = "/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<IngestResponse>>  uploadFile(
            @RequestParam("file") @NotNull MultipartFile file,
            @RequestParam(defaultValue = "uploaded") String source) throws IOException {

        Path tempFile = Files.createTempFile("rag-upload-", "-" + file.getOriginalFilename());
        file.transferTo(tempFile.toFile());

        int chunkCount = ingestionService.ingestDocument(
                tempFile.toString(), source
        );

        Files.delete(tempFile);

        IngestResponse ingestResponse = new IngestResponse(
                chunkCount,
                source
        );
        return ResponseEntity.ok(new ApiResponse<>(ingestResponse));
    }
    @PostMapping(value = "/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<IngestResponse>> ingestPdf(@RequestParam("file") @NotNull MultipartFile file,
                                                    @RequestParam String source) throws IOException {
        String sourceName = source==null?"":source;
        Path tempFile = Files.createTempFile("upload-"+file.getName(), ".pdf");
        file.transferTo(tempFile.toFile());
        int chunkCount = ingestionService.ingestPdf(tempFile.toString(), sourceName);
        Files.deleteIfExists(tempFile);
        IngestResponse ingestResponse = new IngestResponse(
                chunkCount,
                source
        );
        return ResponseEntity.ok(new ApiResponse<>(ingestResponse));
    }
}
