package com.example.ragApplication.controller;

import com.example.ragApplication.model.ApiResponse;
import com.example.ragApplication.model.IngestResponse;
import com.example.ragApplication.model.Ingest;
import com.example.ragApplication.service.IngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/ingest")
public class IngestionController {
    private static final Logger log = LoggerFactory.getLogger(IngestionController.class);

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
            @RequestParam(defaultValue = "uploaded") String source) {

        log.info("Received file upload: {}, size: {} bytes",
                file.getOriginalFilename(), file.getSize());

        // 1. Use the new method that accepts MultipartFile directly
        int chunkCount = ingestionService.ingestDocument(file, source);


        IngestResponse ingestResponse = new IngestResponse(
                chunkCount,
                source
        );
        return ResponseEntity.ok(new ApiResponse<>(ingestResponse));
    }
    @PostMapping(value = "/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<IngestResponse>> ingestPdf(@RequestParam("file") @NotNull MultipartFile file,
                                                    @RequestParam String source)  {
        String sourceName = source==null?"":source;
        int chunkCount = ingestionService.ingestDocument(file, sourceName);
        IngestResponse ingestResponse = new IngestResponse(
                chunkCount,
                source
        );
        return ResponseEntity.ok(new ApiResponse<>(ingestResponse));
    }
}
