package com.gushan.easyai.example.custom;

import com.gushan.easyai.model.ChatRequest;
import com.gushan.easyai.model.ChatResponse;
import com.gushan.easyai.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class CustomController {
    private final AIService aiService;
    
    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        return aiService.chat(request);
    }
} 