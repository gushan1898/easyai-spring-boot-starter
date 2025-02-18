package com.gushan.easyai.service;

import com.gushan.easyai.config.EasyAIProperties;
import com.gushan.easyai.model.ChatRequest;
import com.gushan.easyai.model.ChatResponse;
import com.gushan.easyai.service.impl.OpenAIServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenAIServiceTest {
    private AIService aiService;
    private EasyAIProperties properties;

    @BeforeEach
    void setUp() {
        properties = new EasyAIProperties();
        EasyAIProperties.OpenAI openai = new EasyAIProperties.OpenAI();
        openai.setApiKey("test-api-key");
        properties.setOpenai(openai);
        aiService = new OpenAIServiceImpl(properties);
    }

    @Test
    void testSimpleChat() {
        String response = aiService.chat("你好");
        assertNotNull(response);
        assertTrue(response.length() > 0);
    }

    @Test
    void testChatWithRequest() {
        ChatRequest request = ChatRequest.builder()
                .prompt("你好")
                .build();
        ChatResponse response = aiService.chat(request);
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertNotNull(response.getMessageId());
        assertTrue(response.getTimestamp() > 0);
    }
} 