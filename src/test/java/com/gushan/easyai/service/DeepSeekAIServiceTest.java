package com.gushan.easyai.service;

import com.gushan.easyai.config.EasyAIProperties;
import com.gushan.easyai.model.ChatRequest;
import com.gushan.easyai.model.ChatResponse;
import com.gushan.easyai.service.impl.DeepSeekAIServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class DeepSeekAIServiceTest {
    
    @Mock
    private EasyAIProperties properties;
    
    private AIService aiService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        EasyAIProperties.DeepSeek deepseek = new EasyAIProperties.DeepSeek();
        deepseek.setApiKey("test-api-key");
        when(properties.getDeepseek()).thenReturn(deepseek);
        
        aiService = new DeepSeekAIServiceImpl(properties) {
            @Override
            public ChatResponse chat(ChatRequest request) {
                return ChatResponse.builder()
                        .content("Mock DeepSeek Response")
                        .messageId("mock-" + System.currentTimeMillis())
                        .model(properties.getDeepseek().getModel())
                        .timestamp(System.currentTimeMillis())
                        .build();
            }
        };
    }
    
    @Test
    void testSimpleChat() {
        String response = aiService.chat("测试消息");
        assertNotNull(response);
        assertTrue(response.length() > 0);
    }
    
    @Test
    void testChatWithHistory() {
        ChatRequest request = ChatRequest.builder()
                .prompt("测试消息")
                .history(List.of(
                    ChatRequest.Message.builder()
                        .role("user")
                        .content("你好")
                        .build(),
                    ChatRequest.Message.builder()
                        .role("assistant")
                        .content("你好！有什么我可以帮你的吗？")
                        .build()
                ))
                .build();
                
        ChatResponse response = aiService.chat(request);
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertNotNull(response.getMessageId());
        assertTrue(response.getTimestamp() > 0);
    }
} 