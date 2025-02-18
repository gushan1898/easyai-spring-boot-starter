package com.gushan.easyai;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "easyai.enabled=true",
    "easyai.provider=openai",
    "easyai.openai.api-key=test-key"
})
public abstract class BaseTest {
} 