package com.gushan.easyai.util;

import com.gushan.easyai.exception.AIException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HttpUtilsTest {

    @Test
    void whenInvalidUrl_thenThrowException() {
        assertThrows(AIException.class, () -> 
            HttpUtils.post("invalid-url", "{}", "test-key")
        );
    }

    @Test
    void whenInvalidJson_thenThrowException() {
        assertThrows(AIException.class, () -> 
            HttpUtils.post("https://api.example.com", "invalid-json", "test-key")
        );
    }
} 