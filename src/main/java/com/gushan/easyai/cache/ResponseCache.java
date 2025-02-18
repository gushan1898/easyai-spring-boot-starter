package com.gushan.easyai.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.gushan.easyai.model.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

import java.util.concurrent.TimeUnit;

@Slf4j
@ConditionalOnClass(Caffeine.class)
public class ResponseCache {
    private final Cache<String, ChatResponse> cache;
    
    public ResponseCache(int maximumSize, int expireAfterWrite) {
        this.cache = Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterWrite(expireAfterWrite, TimeUnit.MINUTES)
                .build();
    }
    
    public ChatResponse get(String key) {
        return cache.getIfPresent(key);
    }
    
    public void put(String key, ChatResponse response) {
        cache.put(key, response);
    }
    
    public void invalidate(String key) {
        cache.invalidate(key);
    }
} 