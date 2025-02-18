package com.gushan.easyai.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ChatRequestTest {

    @Test
    void testBuilder() {
        ChatRequest request = ChatRequest.builder()
                .prompt("Hello")
                .history(List.of(
                        ChatRequest.Message.builder()
                                .role("user")
                                .content("Hi")
                                .build()
                ))
                .options(Map.of("temperature", 0.7))
                .build();

        assertEquals("Hello", request.getPrompt());
        assertNotNull(request.getHistory());
        assertEquals(1, request.getHistory().size());
        assertEquals("Hi", request.getHistory().get(0).getContent());
        assertEquals("user", request.getHistory().get(0).getRole());
        assertEquals(0.7, request.getOptions().get("temperature"));
    }
} 