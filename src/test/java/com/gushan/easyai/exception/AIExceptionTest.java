package com.gushan.easyai.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AIExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Test error message";
        AIException exception = new AIException(message);
        
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Test error message";
        Throwable cause = new RuntimeException("Original error");
        AIException exception = new AIException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
} 