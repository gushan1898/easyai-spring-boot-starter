package com.gushan.easyai.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {EasyAIProperties.class})
class EasyAIPropertiesTest {

    @Autowired
    private EasyAIProperties properties;

    @Test
    void testDefaultValues() {
        assertTrue(properties.isEnabled());
        assertEquals("openai", properties.getProvider());
        
        // Test OpenAI defaults
        assertEquals("gpt-3.5-turbo", properties.getOpenai().getModel());
        assertEquals("https://api.openai.com/v1", properties.getOpenai().getBaseUrl());
        assertEquals(30000, properties.getOpenai().getTimeout());
        
        // Test DeepSeek defaults
        assertEquals("deepseek-chat", properties.getDeepseek().getModel());
        assertEquals(0.7, properties.getDeepseek().getTemperature());
        assertEquals(2000, properties.getDeepseek().getMaxTokens());
    }

    @Test
    void testSettersAndGetters() {
        properties.setEnabled(false);
        assertFalse(properties.isEnabled());
        
        properties.setProvider("custom");
        assertEquals("custom", properties.getProvider());
        
        properties.getOpenai().setApiKey("test-key");
        assertEquals("test-key", properties.getOpenai().getApiKey());
    }
} 