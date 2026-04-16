package org.example.earthquakeapp.web;

import org.example.earthquakeapp.dto.AiRequest;
import org.example.earthquakeapp.dto.AiResponse;
import org.example.earthquakeapp.service.GeminiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AiController {

    private final GeminiService geminiService;

    public AiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/ask")
    public ResponseEntity<AiResponse> ask(@RequestBody AiRequest request) {
        return ResponseEntity.ok(geminiService.askQuestion(request.getQuestion()));
    }
}