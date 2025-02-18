package com.gushan.easyai.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gushan.easyai.model.ChatRequest;
import com.gushan.easyai.model.ChatStreamResponse;
import com.gushan.easyai.service.AIService;
import com.gushan.easyai.service.StreamResponseCallback;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class WebSocketChatHandler extends TextWebSocketHandler {
    private final AIService aiService;
    private final ObjectMapper objectMapper;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        ChatRequest request = objectMapper.readValue(message.getPayload(), ChatRequest.class);
        
        aiService.chatStream(request, new StreamResponseCallback() {
            @Override
            public void onResponse(ChatStreamResponse response) {
                try {
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
                } catch (Exception e) {
                    log.error("发送消息失败", e);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                try {
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
                        Map.of("error", throwable.getMessage()))));
                } catch (Exception e) {
                    log.error("发送错误消息失败", e);
                }
            }

            @Override
            public void onComplete() {
                try {
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
                        Map.of("type", "complete"))));
                } catch (Exception e) {
                    log.error("发送完成消息失败", e);
                }
            }
        });
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebSocket连接已建立: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("WebSocket连接已关闭: {}, 状态: {}", session.getId(), status);
    }
} 