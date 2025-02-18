package com.gushan.easyai.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gushan.easyai.service.AIService;
import com.gushan.easyai.websocket.WebSocketChatHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@ConditionalOnClass(WebSocketHandler.class)
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    private final AIService aiService;
    private final ObjectMapper objectMapper;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new WebSocketChatHandler(aiService, objectMapper), "/ws/chat")
                .setAllowedOrigins("*");
    }
} 