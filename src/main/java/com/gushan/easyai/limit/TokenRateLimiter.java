package com.gushan.easyai.limit;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ConditionalOnClass(RateLimiter.class)
public class TokenRateLimiter {
    private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();
    
    public boolean tryAcquire(String provider, double tokensPerSecond) {
        RateLimiter limiter = limiters.computeIfAbsent(provider, 
            k -> RateLimiter.create(tokensPerSecond));
        return limiter.tryAcquire();
    }
    
    public void acquire(String provider, double tokensPerSecond) {
        RateLimiter limiter = limiters.computeIfAbsent(provider, 
            k -> RateLimiter.create(tokensPerSecond));
        limiter.acquire();
    }
} 