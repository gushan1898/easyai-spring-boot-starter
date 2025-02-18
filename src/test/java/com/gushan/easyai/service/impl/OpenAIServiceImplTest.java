package com.gushan.easyai.service.impl;

import com.gushan.easyai.BaseTest;
import com.gushan.easyai.config.EasyAIProperties;
import com.gushan.easyai.exception.AIException;
import com.gushan.easyai.model.ChatRequest;
import com.gushan.easyai.model.ChatResponse;
import com.gushan.easyai.model.ChatStreamResponse;
import com.gushan.easyai.model.ImageGenerationRequest;
import com.gushan.easyai.model.ImageGenerationResponse;
import com.gushan.easyai.service.StreamResponseCallback;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpenAIServiceImplTest extends BaseTest {

    @Mock
    private EasyAIProperties properties;

    private OpenAIServiceImpl service;

    @BeforeEach
    void setUp() {
        EasyAIProperties.OpenAI openai = new EasyAIProperties.OpenAI();
        openai.setApiKey("test-key");
        when(properties.getOpenai()).thenReturn(openai);
        
        service = new OpenAIServiceImpl(properties);
    }

    @Test
    void testSimpleChat() {
        String response = service.chat("Hello");
        assertNotNull(response);
    }

    @Test
    void testChatWithHistory() {
        ChatRequest request = ChatRequest.builder()
                .prompt("Hello again")
                .history(List.of(
                        ChatRequest.Message.builder()
                                .role("user")
                                .content("Hello")
                                .build(),
                        ChatRequest.Message.builder()
                                .role("assistant")
                                .content("Hi there!")
                                .build()
                ))
                .build();

        ChatResponse response = service.chat(request);
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertNotNull(response.getMessageId());
        assertTrue(response.getTimestamp() > 0);
    }

    @Test
    void whenApiKeyIsMissing_thenThrowException() {
        when(properties.getOpenai()).thenReturn(new EasyAIProperties.OpenAI());
        
        assertThrows(AIException.class, () -> service.chat("Hello"));
    }

    @Test
    void testChatStream() throws InterruptedException {
        List<String> responses = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        
        ChatRequest request = ChatRequest.builder()
                .prompt("Hello")
                .build();
                
        service.chatStream(request, new StreamResponseCallback() {
            @Override
            public void onResponse(ChatStreamResponse response) {
                responses.add(response.getContent());
            }
            
            @Override
            public void onError(Throwable throwable) {
                fail(throwable.getMessage());
            }
            
            @Override
            public void onComplete() {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        assertFalse(responses.isEmpty());
    }

    @Test
    void testGenerateImage() {
        ImageGenerationRequest request = ImageGenerationRequest.builder()
                .prompt("A cute cat")
                .width(512)
                .height(512)
                .numberOfImages(1)
                .build();
                
        ImageGenerationResponse response = service.generateImage(request);
        
        assertNotNull(response);
        assertFalse(response.getImageUrls().isEmpty());
        assertEquals(1, response.getImageUrls().size());
    }
} 