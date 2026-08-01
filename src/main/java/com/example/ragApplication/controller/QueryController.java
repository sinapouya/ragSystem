package com.example.ragApplication.controller;

import com.example.ragApplication.model.Answer;
import com.example.ragApplication.model.ApiResponse;
import com.example.ragApplication.model.Question;
import com.example.ragApplication.service.RagService;
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
