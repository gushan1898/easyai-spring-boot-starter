package com.gushan.easyai.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gushan.easyai.exception.AIException;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class XunfeiWebSocketClient extends WebSocketClient {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CompletableFuture<String> responseFuture = new CompletableFuture<>();
    private final StringBuilder responseBuilder = new StringBuilder();

    public XunfeiWebSocketClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        log.debug("WebSocket连接已建立");
    }

    @Override
    public void onMessage(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            int code = jsonNode.get("header").get("code").asInt();
            
            if (code != 0) {
                responseFuture.completeExceptionally(new AIException("讯飞API返回错误: " + code));
                return;
            }

            JsonNode payload = jsonNode.get("payload");
            String text = payload.get("choices").get("text").get(0).asText();
            responseBuilder.append(text);

            // 判断是否是最后一条消息
            if (payload.get("status").asInt() == 2) {
                responseFuture.complete(responseBuilder.toString());
                this.close();
            }
        } catch (Exception e) {
            responseFuture.completeExceptionally(e);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.debug("WebSocket连接已关闭: code={}, reason={}, remote={}", code, reason, remote);
        if (!responseFuture.isDone()) {
            responseFuture.completeExceptionally(new AIException("WebSocket连接已关闭: " + reason));
        }
    }

    @Override
    public void onError(Exception ex) {
        log.error("WebSocket发生错误", ex);
        responseFuture.completeExceptionally(ex);
    }

    public CompletableFuture<String> getResponseFuture() {
        return responseFuture;
    }
} 