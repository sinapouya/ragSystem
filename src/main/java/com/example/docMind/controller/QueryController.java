package com.example.docMind.controller;

import com.example.docMind.model.Answer;
import com.example.docMind.model.ApiResponse;
import com.example.docMind.model.Question;
import com.example.docMind.service.RagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/query")
public class QueryController {
    private final RagService ragService;

    public QueryController(RagService ragService) {
        this.ragService = ragService;
    }
    @PostMapping("/ask")
    public ResponseEntity<ApiResponse<Answer>> ask(@RequestBody Question question) {
        Answer answer  = new Answer(ragService.ask(question));
        return ResponseEntity.ok(new ApiResponse<>(answer));
    }

}
